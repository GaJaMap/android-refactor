package com.pg.gajamap.presentation.client


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.pg.gajamap.data.model.ClientEntity
import com.pg.gajamap.data.model.request.DeleteRequest
import com.pg.gajamap.databinding.FragmentCustomerEditBinding
import com.pg.gajamap.util.UiState
import com.pg.gajamap.util.collectLatestStateFlow
import com.pg.gajamap.util.decoration.CustomerListVerticalItemDecoration
import com.pg.gajamap.util.deleteCheckDialog
import com.pg.gajamap.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerEditFragment : Fragment() {
    private var _binding: FragmentCustomerEditBinding? = null
    private val binding get() = _binding!!

    private val customerViewModel: CustomerViewModel by viewModels()
    private lateinit var editListAdapter: EditListAdapter

    private val args by navArgs<CustomerEditFragmentArgs>()

    private var clientList = ArrayList<ClientEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observer()

        binding.checkEvery.setOnCheckedChangeListener { _, isSelected ->
            editListAdapter.checkAll(clientList, isSelected)
            binding.topTvNumber1.text = clientList.count { it.isChecked }.toString()
        }

        binding.topDeleteBtn.setOnClickListener {
            deleteCheckDialog(
                "해당 고객을 삭제하시겠습니까?",
                "고객을 삭제하시면 영구 삭제되어 복구할 수 없습니다."
            ) {

                val checkedClientIds = clientList.filter { it.isChecked }.map { it.client.clientId }
                val deleteRequest = DeleteRequest(checkedClientIds)

                customerViewModel.deleteAnyClient(
                    args.group.groupId,
                    deleteRequest
                )
            }
        }
    }

    private fun observer() {
        collectLatestStateFlow(customerViewModel.getClient(args.group.groupId, "")) { uiState ->
            when (uiState) {
                is UiState.Loading -> {}
                is UiState.Success -> {
                    val clientEntities: List<ClientEntity> = uiState.data.clients.map { client ->
                        ClientEntity(client)
                    }
                    clientList = clientEntities as ArrayList<ClientEntity>
                    editListAdapter.submitList(clientEntities)

                    binding.topTvNumber2.text = clientList.count().toString()
                }

                is UiState.Failure -> requireContext().toast(uiState.error!!)
            }
        }

        collectLatestStateFlow(customerViewModel.deleteResult) { result ->
            result?.let {
                when (it) {
                    is UiState.Loading -> {
                    }

                    is UiState.Success -> {
                        requireContext().toast("삭제 완료")
                        findNavController().popBackStack()
                    }

                    is UiState.Failure -> {
                        requireContext().toast(it.error!!)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        editListAdapter = EditListAdapter()
        binding.listRv.apply {
            setHasFixedSize(true)
            addItemDecoration(CustomerListVerticalItemDecoration())
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = editListAdapter
        }
        editListAdapter.setOnItemClickListener {
            binding.topTvNumber1.text = clientList.count { it.isChecked }.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}