package com.pg.gajamap.presentation.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.pg.gajamap.R
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.databinding.FragmentEditDetailBinding
import com.pg.gajamap.util.Constants.imageUrlPrefix
import com.pg.gajamap.util.UiState
import com.pg.gajamap.util.collectLatestStateFlow
import com.pg.gajamap.util.getRealPathFromURI
import com.pg.gajamap.util.hide
import com.pg.gajamap.util.show
import com.pg.gajamap.util.showNavigationDialog
import com.pg.gajamap.util.toTextRequestBody
import com.pg.gajamap.util.toast
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@AndroidEntryPoint
class EditDetailFragment : Fragment() {
    private var _binding: FragmentEditDetailBinding? = null
    private val binding get() = _binding!!

    private val detailViewModel: DetailViewModel by viewModels()
    private val args by navArgs<EditDetailFragmentArgs>()

    private var imageBody: MultipartBody.Part? = null
    private var isBasicImage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val client = args.client

        observer()
        viewBinding(client)

        detailViewModel.updateProfileName(args.client.clientName)
        detailViewModel.updateProfilePhone(args.client.phoneNumber)

        binding.infoProfileNameEt.addTextChangedListener { text ->
            detailViewModel.updateProfileName(text.toString())
        }

        binding.infoProfilePhoneEt.addTextChangedListener { text ->
            detailViewModel.updateProfilePhone(text.toString())
        }

        binding.infoProfileCameraBtn.setOnClickListener {
            val items = arrayOf("앨범에서 사진 선택", "기본 이미지로 변경")
            requireContext().showNavigationDialog("프로필 사진", items) { selectedItem ->
                when (selectedItem) {
                    "앨범에서 사진 선택" -> {
                        val intentImage = Intent(Intent.ACTION_PICK)
                        intentImage.type = MediaStore.Images.Media.CONTENT_TYPE
                        getContent.launch(intentImage)
                    }

                    "기본 이미지로 변경" -> {
                        binding.infoProfileImg.load(R.drawable.profile_img_origin)
                        imageBody = null
                        isBasicImage = true
                    }
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            putClientData(client, imageBody, isBasicImage)
        }

        binding.infoProfileAddressChangeBtn.setOnClickListener {
            val action =
                EditDetailFragmentDirections.actionEditDetailFragmentToAddressEditFragment(client)
            findNavController().navigate(action)
        }

        binding.topBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private val getContent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                val file = File(it.getRealPathFromURI(requireContext()))
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("clientImage", file.name, requestFile)

                imageBody = body
                isBasicImage = false
                binding.infoProfileImg.load(it)
            }
        }
    }

    private fun observer() {
        collectLatestStateFlow(detailViewModel.putResult) { result ->
            result?.let {
                when (it) {
                    is UiState.Loading -> {
                        binding.btnSubmit.text = ""
                        binding.progress.show(requireActivity())
                    }

                    is UiState.Success -> {
                        binding.btnSubmit.text = "확인"
                        binding.progress.hide(requireActivity())

                        findNavController().navigate(
                            EditDetailFragmentDirections.actionEditDetailFragmentToNavigationCustomer()
                        )
                    }

                    is UiState.Failure -> {
                        binding.btnSubmit.text = "확인"
                        binding.progress.hide(requireActivity())
                        requireContext().toast(it.error!!)
                    }
                }
            }
        }

        collectLatestStateFlow(detailViewModel.isButtonEnabled) { isEnabled ->
            binding.btnSubmit.isEnabled = isEnabled
        }
    }

    private fun viewBinding(client: Client) {
        binding.btnSubmit.isEnabled = true
        binding.infoProfileNameEt.setText(client.clientName)
        binding.infoProfileAddressTv1.text = client.address.mainAddress
        binding.infoProfileAddressTv2.setText(client.address.detail)
        binding.infoProfilePhoneEt.setText(PhoneNumberUtils.formatNumber(client.phoneNumber))
        binding.infoProfilePhoneEt.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        binding.infoProfileImg.load(imageUrlPrefix + client.image?.filePath) {
            error(R.drawable.profile_img_origin)
        }
    }

    private fun putClientData(
        client: Client,
        imageBody: MultipartBody.Part?,
        isBasicImage: Boolean
    ) {
        val clientName = binding.infoProfileNameEt.text.toString().toTextRequestBody()
        val groupId = client.groupInfo.groupId
        val phoneNumber =
            binding.infoProfilePhoneEt.text.toString().replace("-", "").toTextRequestBody()
        val mainAddress = binding.infoProfileAddressTv1.text.toString().toTextRequestBody()
        val detail = binding.infoProfileAddressTv2.text.toString().toTextRequestBody()
        val latitude = client.location.latitude.toString().toTextRequestBody()
        val longitude = client.location.longitude.toString().toTextRequestBody()

        detailViewModel.putClient(
            groupId,
            client.clientId,
            clientName,
            groupId.toString().toTextRequestBody(),
            phoneNumber,
            mainAddress,
            detail,
            latitude,
            longitude,
            imageBody,
            isBasicImage.toTextRequestBody()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}