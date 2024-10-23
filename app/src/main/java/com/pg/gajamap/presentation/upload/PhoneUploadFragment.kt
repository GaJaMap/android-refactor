package com.pg.gajamap.presentation.upload

import android.Manifest
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.pg.gajamap.R
import com.pg.gajamap.data.model.PhoneEntity
import com.pg.gajamap.data.model.request.upload.Client
import com.pg.gajamap.databinding.FragmentPhoneUploadBinding
import com.pg.gajamap.util.UiState
import com.pg.gajamap.util.collectLatestStateFlow
import com.pg.gajamap.util.decoration.PhoneListVerticalItemDecoration
import com.pg.gajamap.util.hide
import com.pg.gajamap.util.show
import com.pg.gajamap.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneUploadFragment : Fragment() {
    private var _binding: FragmentPhoneUploadBinding? = null
    private val binding get() = _binding!!

    private val uploadViewModel: UploadViewModel by viewModels()
    private lateinit var phoneListAdapter: PhoneListAdapter

    private var phoneList = ArrayList<PhoneEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observer()
        uploadViewModel.checkGroup()

        requestPermission {
            getPhoneList()
        }

        binding.settingPhoneSearchEt.addTextChangedListener {
            filterClientList(it.toString())
        }

        binding.settingPhoneSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(adapterView: AdapterView<*>?) {}

                override fun onItemSelected(
                    adapterView: AdapterView<*>?, view: View?,
                    pos: Int, id: Long
                ) {
                    uploadViewModel.updateGroupId(pos)
                }
            }

        binding.settingPhoneCheckEvery.setOnCheckedChangeListener { _, isChecked ->
            phoneListAdapter.checkAll(phoneList, isChecked)
            uploadViewModel.updateIsChecked(phoneList.any { it.isChecked })
            binding.topTvNumber1.text = phoneList.count { it.isChecked }.toString()
        }

        binding.btnSubmit.setOnClickListener {
            val selectedPhoneList = phoneList.filter { it.isChecked }
            val clients = mutableListOf<Client>()

            selectedPhoneList.forEach { phoneEntity ->
                val name = phoneEntity.name
                val number = phoneEntity.number

                val client = Client(name, number)
                clients.add(client)
            }

            uploadViewModel.postUploadClient(clients)
        }

        binding.topBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observer() {
        collectLatestStateFlow(uploadViewModel.checkResult) {
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
                            binding.settingPhoneSpinner.adapter = adapter

                            val groupState = uploadViewModel.groupState().value
                            val selectedIndex = it.data.indexOfFirst { groupEntity ->
                                groupEntity.groupId == groupState.groupId
                            } + 1

                            binding.settingPhoneSpinner.setSelection(selectedIndex)
                        }
                }

                is UiState.Failure -> {}
            }
        }

        collectLatestStateFlow(uploadViewModel.postResult) { result ->
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

        collectLatestStateFlow(uploadViewModel.isButtonEnabled) { isEnabled ->
            binding.btnSubmit.isEnabled = isEnabled
        }
    }

    private fun setupRecyclerView() {
        phoneListAdapter = PhoneListAdapter()
        binding.phoneListRv.apply {
            setHasFixedSize(true)
            addItemDecoration(PhoneListVerticalItemDecoration())
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = phoneListAdapter
        }
        phoneListAdapter.setOnItemClickListener {
            uploadViewModel.updateIsChecked(phoneList.any { it.isChecked })
            binding.topTvNumber1.text = phoneList.count { it.isChecked }.toString()
        }
    }

    private fun filterClientList(searchText: String) {
        val filteredList = phoneList.filter { it.name.contains(searchText, ignoreCase = true) }
        phoneListAdapter.submitList(filteredList)
    }

    private fun requestPermission(logic: () -> Unit) {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    logic()
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    requireContext().toast("권한 거부\n$deniedPermissions")
                }
            })
            .setDeniedMessage("권한을 허용해주세요. [설정] > [앱 및 알림] > [고급] > [앱 권한]")
            .setPermissions(
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_CONTACTS
            )
            .check()
    }

    private fun getPhoneList() {
        requireContext().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            val list = ArrayList<PhoneEntity>()

            val idIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val id = cursor.getInt(idIndex)
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex).replace("-", "")

                if (name.length <= 10 && !list.any { it.name == name && it.number == number }) {
                    list.add(PhoneEntity(id, name, number))
                }
            }

            phoneList = list
            phoneListAdapter.submitList(phoneList)
            binding.topTvNumber2.text = phoneList.count().toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}