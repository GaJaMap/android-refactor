package com.pg.gajamap.presentation.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.pg.gajamap.R
import com.pg.gajamap.databinding.FragmentCustomerBinding
import com.pg.gajamap.util.Constants.SEARCH_CLIENTS_TIME_DELAY
import com.pg.gajamap.util.SortType
import com.pg.gajamap.util.UiState
import com.pg.gajamap.util.collectLatestStateFlow
import com.pg.gajamap.util.decoration.CustomerListVerticalItemDecoration
import com.pg.gajamap.util.getTextChangeStateFlow
import com.pg.gajamap.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

@AndroidEntryPoint
class CustomerFragment : Fragment() {
    private var _binding: FragmentCustomerBinding? = null
    private val binding get() = _binding!!

    private val customerViewModel: CustomerViewModel by viewModels()
    private lateinit var customerListAdapter: CustomerListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observer()
        initListeners()
        setupBackPressedCallback()

        customerViewModel.saveSortType(SortType.NEWEST)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observer() {
        collectLatestStateFlow(customerViewModel.groupState) { groupState ->
            binding.tvSearch.text = groupState.groupName
        }

        collectLatestStateFlow(
            combine(
                binding.etSearch.getTextChangeStateFlow()
                    .debounce(SEARCH_CLIENTS_TIME_DELAY),
                customerViewModel.groupState
            ) { query, groupState ->
                Pair(query, groupState.groupId)
            }
                .flatMapLatest { (query, groupId) ->
                    customerViewModel.getClient(groupId, query)
                }
        ) { uiState ->
            when (uiState) {
                is UiState.Loading -> {}
                is UiState.Success -> customerListAdapter.sortData(
                    uiState.data.clients,
                    customerViewModel.currentSortType.first()
                )

                is UiState.Failure -> requireContext().toast(uiState.error!!)
            }
        }

        collectLatestStateFlow(customerViewModel.createMemo) { result ->
            result?.let {
                when (it) {
                    is UiState.Loading -> {
                    }

                    is UiState.Success -> {
                    }

                    is UiState.Failure -> {
                        requireContext().toast(it.error!!)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        customerListAdapter = CustomerListAdapter()
        binding.customerRecyclerview.apply {
            setHasFixedSize(true)
            addItemDecoration(CustomerListVerticalItemDecoration())
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = customerListAdapter
        }
        customerListAdapter.setOnItemClickListener {
            val action =
                CustomerFragmentDirections.actionNavigationCustomerToDetailCustomerFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun initListeners() {
        binding.clSearch.setOnClickListener {
            val action = CustomerFragmentDirections.actionNavigationCustomerToGroupSheetFragment()
            findNavController().navigate(action)
        }

        binding.fragmentEditBtn.setOnClickListener {
            val selectedGroup = customerViewModel.groupState.replayCache[0]
            if (selectedGroup.groupId == -1L) {
                Snackbar.make(
                    requireView(),
                    "전체 그룹은 편집 기능을 사용하지 못합니다.",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                val action =
                    CustomerFragmentDirections.actionNavigationCustomerToCustomerEditFragment(
                        selectedGroup
                    )
                findNavController().navigate(action)
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val sortType = when (checkedId) {
                R.id.fragment_list_category1 -> SortType.NEWEST
                R.id.fragment_list_category2 -> SortType.OLDEST
                R.id.fragment_list_category3 -> SortType.DISTANCE
                else -> SortType.NEWEST
            }
            customerViewModel.saveSortType(sortType)
        }
    }

    private fun setupBackPressedCallback() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this.isEnabled = false
                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}