package com.pg.gajamap.presentation.group

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pg.gajamap.R
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.databinding.FragmentGroupSheetBinding
import com.pg.gajamap.databinding.GroupDialogBinding
import com.pg.gajamap.util.UiState
import com.pg.gajamap.util.collectLatestStateFlow
import com.pg.gajamap.util.deleteCheckDialog
import com.pg.gajamap.util.showGroupDialog
import com.pg.gajamap.util.toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class GroupSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentGroupSheetBinding? = null
    private val binding get() = _binding!!

    private val groupViewModel: GroupViewModel by viewModels()
    private lateinit var groupListAdapter: GroupListAdapter

    @Inject
    lateinit var storage: GJMSharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.BottomSheetTheme)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observer()

        groupViewModel.checkGroup()

        binding.btnAddgroup.setOnClickListener {
            val createDialog = showGroupDialog(layoutInflater, "그룹 추가하기")
            handleGroupDialog(createDialog) {
                groupViewModel.createGroup(it)
            }
        }
    }

    private fun observer() {
        collectLatestStateFlow(storage.groupName) { groupName ->
            binding.tvAddgroupMain.text = groupName
        }

        collectLatestStateFlow(groupViewModel.checkResult) {
            when (it) {
                is UiState.Loading -> {}
                is UiState.Success -> {
                    groupListAdapter.submitList(it.data.toMutableList())
                }

                is UiState.Failure -> {
                    requireContext().toast(it.error!!)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        groupListAdapter = GroupListAdapter(groupViewModel)
        binding.groupRecyclerview.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = groupListAdapter
        }
        groupListAdapter.setOnItemClickListener {
            groupViewModel.saveGroupData(it.groupId, it.groupName)
            findNavController().popBackStack()
        }
        groupListAdapter.setOnModifyClickListener { group ->
            val modifyDialog = showGroupDialog(layoutInflater, "그룹 수정하기")
            handleGroupDialog(modifyDialog) {
                groupViewModel.modifyGroup(group.groupId, it)

                if (group.groupId == groupViewModel.currentGroupId().value) {
                    groupViewModel.saveGroupData(group.groupId, it)
                }
            }
        }
        groupListAdapter.setOnDeleteClickListener {
            deleteCheckDialog(
                "해당 그룹을 삭제하시겠습니까?",
                "그룹을 삭제하시면 영구 삭제되어 복구할 수 없습니다."
            ) {
                groupViewModel.deleteGroup(it.groupId)

                if (it.groupId == groupViewModel.currentGroupId().value) {
                    groupViewModel.saveGroupData(-1, "전체")
                }
                requireContext().toast("그룹 삭제 완료")
            }
        }
    }

    private fun handleGroupDialog(
        dialog: Pair<AlertDialog, GroupDialogBinding>,
        nameCallback: (String) -> Unit
    ) {
        val (alertDialog, binding) = dialog

        binding.ivClose.setOnClickListener { alertDialog.dismiss() }

        binding.btnDialogSubmit.setOnClickListener {
            val etName = binding.etName.text.toString()

            if (etName == "전체" || etName.isEmpty()) {
                requireContext().toast("사용할 수 없는 그룹 이름입니다")
            } else {
                nameCallback(etName)
                alertDialog.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}