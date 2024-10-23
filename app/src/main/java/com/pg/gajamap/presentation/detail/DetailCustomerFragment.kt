package com.pg.gajamap.presentation.detail

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.pg.gajamap.BuildConfig
import com.pg.gajamap.R
import com.pg.gajamap.data.model.request.MemoRequest
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.databinding.FragmentCustomerDetailBinding
import com.pg.gajamap.databinding.MemoDialogBinding
import com.pg.gajamap.util.Constants.imageUrlPrefix
import com.pg.gajamap.util.UiState
import com.pg.gajamap.util.collectLatestStateFlow
import com.pg.gajamap.util.deleteCheckDialog
import com.pg.gajamap.util.getStaticMapUrl
import com.pg.gajamap.util.hide
import com.pg.gajamap.util.launchKakaoNavi
import com.pg.gajamap.util.launchNaverNavi
import com.pg.gajamap.util.show
import com.pg.gajamap.util.showMemoDialog
import com.pg.gajamap.util.showNavigationDialog
import com.pg.gajamap.util.toast
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DetailCustomerFragment : Fragment() {
    private var _binding: FragmentCustomerDetailBinding? = null
    private val binding get() = _binding!!

    private val detailViewModel: DetailViewModel by viewModels()
    private val args by navArgs<DetailCustomerFragmentArgs>()

    private lateinit var memoPagingAdapter: MemoPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val client = args.client

        detailViewModel.getMemosPaging(client.clientId)

        setupRecyclerView()
        observer()
        viewBinding(client)
        setupLoadState()

        binding.itemProfileCarBtn.setOnClickListener {
            val items = arrayOf("카카오 내비", "네이버 내비")
            val currentTime = getCurrentTime()

            requireContext().showNavigationDialog(null, items) { selectedItem ->
                when (selectedItem) {
                    "카카오 내비" -> {
                        requireContext().launchKakaoNavi(
                            client.clientName,
                            client.location.longitude,
                            client.location.latitude
                        )
                        detailViewModel.createMemo(
                            client.clientId,
                            MemoRequest(currentTime, "NAVIGATION")
                        )
                    }

                    "네이버 내비" -> {
                        requireContext().launchNaverNavi(
                            client.clientName,
                            client.location.longitude,
                            client.location.latitude
                        )
                        detailViewModel.createMemo(
                            client.clientId,
                            MemoRequest(currentTime, "NAVIGATION")
                        )
                    }
                }
            }
        }

        binding.itemProfilePhoneBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${client.phoneNumber}")
            startActivity(intent)

            val currentTime = getCurrentTime()
            detailViewModel.createMemo(client.clientId, MemoRequest(currentTime, "CALL"))
        }

        binding.itemMemoAddBtn.setOnClickListener {
            val createDialog = showMemoDialog(layoutInflater, "메모 추가하기")
            handleMemoDialog(createDialog) {
                detailViewModel.createMemo(client.clientId, MemoRequest(it, "MESSAGE"))
            }
        }

        binding.topModifyBtn.setOnClickListener {
            val action =
                DetailCustomerFragmentDirections.actionDetailCustomerFragmentToEditDetailFragment(
                    client
                )
            findNavController().navigate(action)
        }

        binding.topDeleteBtn.setOnClickListener {
            deleteCheckDialog(
                "해당 고객을 삭제하시겠습니까?",
                "고객을 삭제하시면 영구 삭제되어 복구할 수 없습니다."
            ) {
                detailViewModel.deleteClient(client.groupInfo.groupId, client.clientId)
            }
        }

        binding.topBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observer() {
        collectLatestStateFlow(detailViewModel.deleteResult) { result ->
            result?.let {
                when (it) {
                    is UiState.Loading -> {
                    }

                    is UiState.Success -> {
                        findNavController().popBackStack()
                    }

                    is UiState.Failure -> {
                        requireContext().toast(it.error!!)
                    }
                }
            }
        }

        collectLatestStateFlow(detailViewModel.memosPaging) {
            memoPagingAdapter.submitData(it)
        }

        collectLatestStateFlow(detailViewModel.createMemoResult) { result ->
            result?.let {
                when (it) {
                    is UiState.Loading -> {
                    }

                    is UiState.Success -> {
                        detailViewModel.getMemosPaging(args.client.clientId)
                        binding.recyclerView.scrollToPosition(0)
                    }

                    is UiState.Failure -> {
                        requireContext().toast(it.error!!)
                    }
                }
            }
        }

        collectLatestStateFlow(detailViewModel.deleteMemoResult) { result ->
            result?.let {
                when (it) {
                    is UiState.Loading -> {
                    }

                    is UiState.Success -> {
                        detailViewModel.getMemosPaging(args.client.clientId)
                        binding.recyclerView.scrollToPosition(0)
                    }

                    is UiState.Failure -> {
                        requireContext().toast(it.error!!)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        memoPagingAdapter = MemoPagingAdapter()
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = memoPagingAdapter.withLoadStateFooter(
                footer = MemoLoadStateAdapter(memoPagingAdapter)
            )
        }
        memoPagingAdapter.setOnItemClickListener {
            detailViewModel.deleteMemo(it.memoId)
        }
    }

    private fun viewBinding(client: Client) {
        binding.infoProfileNameTv.text = client.clientName
        binding.infoGroupNameTv.text = client.groupInfo.groupName
        binding.infoProfileAddressTv1.text = client.address.mainAddress
        binding.infoProfileAddressTv2.text = client.address.detail
        binding.infoProfilePhoneTv.text = PhoneNumberUtils.formatNumber(client.phoneNumber)
        binding.infoProfileImg.load(imageUrlPrefix + client.image?.filePath) {
            error(R.drawable.profile_img_origin)
        }
        
        val mapUrl = getStaticMapUrl(
            client.location.latitude,
            client.location.longitude,
            BuildConfig.GOOGLE_MAP_KEY
        )

        if (client.location.latitude != null) {
            binding.mapImage.load(mapUrl) {
                error(R.drawable.ic_location_not_found)
            }
        } else {
            binding.mapImage.setBackgroundResource(R.color.light_gray)
        }
    }

    private fun handleMemoDialog(
        dialog: Pair<AlertDialog, MemoDialogBinding>,
        nameCallback: (String) -> Unit
    ) {
        val (alertDialog, binding) = dialog

        binding.ivClose.setOnClickListener { alertDialog.dismiss() }

        binding.btnDialogSubmit.setOnClickListener {
            val etName = binding.etName.text.toString()

            nameCallback(etName)
            alertDialog.dismiss()
        }
    }

    private fun setupLoadState() {
        memoPagingAdapter.addLoadStateListener { combinedLoadStates ->
            val loadState = combinedLoadStates.source
            if (loadState.refresh is LoadState.Loading) {
                binding.recyclerView.hide(requireActivity())
            }

            if (loadState.refresh is LoadState.NotLoading) {
                binding.recyclerView.show(requireActivity())
            }
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("yy년 M월 d일 HH시 mm분", Locale.getDefault()).format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}