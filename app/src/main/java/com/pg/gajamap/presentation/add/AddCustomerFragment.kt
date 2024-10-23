package com.pg.gajamap.presentation.add


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.pg.gajamap.R
import com.pg.gajamap.data.model.AddressInfo
import com.pg.gajamap.databinding.FragmentCustomerAddBinding
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
class AddCustomerFragment : Fragment() {
    private var _binding: FragmentCustomerAddBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<AddCustomerFragmentArgs>()
    private val addViewModel: AddViewModel by viewModels()

    private var groupId: Long = -1L
    private var imageBody: MultipartBody.Part? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val addressInfo = args.addressInfo

        observer()
        addViewModel.checkGroup()

        binding.infoProfileNameEt.addTextChangedListener { text ->
            addViewModel.updateProfileName(text.toString())
        }

        binding.infoProfilePhoneEt.addTextChangedListener { text ->
            addViewModel.updateProfilePhone(text.toString())
        }

        binding.infoProfileAddressTv1.text = addressInfo.address
        binding.infoProfilePhoneEt.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        binding.btnSubmit.setOnClickListener {
            postClientData(addressInfo, imageBody)
        }

        binding.infoProfileGroup.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(adapterView: AdapterView<*>?) {}

                override fun onItemSelected(
                    adapterView: AdapterView<*>?, view: View?,
                    pos: Int, id: Long
                ) {
                    groupId = if (pos != 0) {
                        addViewModel.checkItems[pos - 1].groupId
                    } else {
                        -1L
                    }
                    addViewModel.updateGroupId(groupId)
                }
            }

        binding.topBackBtn.setOnClickListener {
            findNavController().popBackStack()
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
                    }
                }
            }
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
                binding.infoProfileImg.load(it)
            }
        }
    }

    private fun observer() {
        collectLatestStateFlow(addViewModel.checkResult) {
            when (it) {
                is UiState.Loading -> {}
                is UiState.Success -> {
                    val groupNames = mutableListOf<String>().apply {
                        add("그룹 선택")
                        it.data.forEach { groupEntity ->
                            add(groupEntity.groupName)
                        }
                    }
                    ArrayAdapter(requireContext(), R.layout.spinner_list, groupNames)
                        .also { adapter ->
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.infoProfileGroup.adapter = adapter

                            val groupState = addViewModel.groupState().value
                            val selectedIndex = it.data.indexOfFirst { groupEntity ->
                                groupEntity.groupId == groupState.groupId
                            } + 1
                            binding.infoProfileGroup.setSelection(selectedIndex)
                        }
                }

                is UiState.Failure -> {}
            }
        }

        collectLatestStateFlow(addViewModel.postResult) { result ->
            result?.let {
                when (it) {
                    is UiState.Loading -> {
                        binding.btnSubmit.text = ""
                        binding.progress.show(requireActivity())
                    }

                    is UiState.Success -> {
                        binding.btnSubmit.text = "확인"
                        binding.progress.hide(requireActivity())
                        findNavController().popBackStack()
                    }

                    is UiState.Failure -> {
                        binding.btnSubmit.text = "확인"
                        binding.progress.hide(requireActivity())
                        requireContext().toast(it.error!!)
                    }
                }
            }
        }

        collectLatestStateFlow(addViewModel.isButtonEnabled) { isEnabled ->
            binding.btnSubmit.isEnabled = isEnabled
        }
    }

    private fun postClientData(addressInfo: AddressInfo, imageBody: MultipartBody.Part?) {
        val clientName = binding.infoProfileNameEt.text.toString().toTextRequestBody()
        val groupId = groupId.toString().toTextRequestBody()
        val phoneNumber =
            binding.infoProfilePhoneEt.text.toString().replace("-", "").toTextRequestBody()
        val mainAddress = binding.infoProfileAddressTv1.text.toString().toTextRequestBody()
        val detail = binding.infoProfileAddressTv2.text.toString().toTextRequestBody()
        val latitude = addressInfo.latitude.toString().toTextRequestBody()
        val longitude = addressInfo.longitude.toString().toTextRequestBody()
        val isBasicImage = (imageBody == null).toTextRequestBody()

        addViewModel.postClient(
            clientName, groupId, phoneNumber, mainAddress, detail,
            latitude, longitude, imageBody, isBasicImage
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}