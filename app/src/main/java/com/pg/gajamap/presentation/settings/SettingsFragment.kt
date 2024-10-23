package com.pg.gajamap.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pg.gajamap.databinding.FragmentSettingsBinding
import com.pg.gajamap.presentation.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressedCallback()

        binding.settingLogoutTv.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            settingsViewModel.clear()
        }

        lifecycleScope.launch {
            settingsViewModel.email.collect { email ->
                binding.settingEmailTv.text = email
            }
        }

        lifecycleScope.launch {
            settingsViewModel.createdDate.collect { createdDate ->
                binding.settingDateTv.text = createdDate
            }
        }

        lifecycleScope.launch {
            settingsViewModel.authority.collect { authority ->
                binding.settingLevelTv.text = authority
            }
        }

        binding.settingExcelTv.setOnClickListener {
            val action = SettingsFragmentDirections.actionNavigationSettingsToExcelUploadFragment()
            findNavController().navigate(action)
        }

        binding.settingKakaoTv.setOnClickListener {
            val action = SettingsFragmentDirections.actionNavigationSettingsToKakaoUploadFragment()
            findNavController().navigate(action)
        }

        binding.settingPhoneTv.setOnClickListener {
            val action = SettingsFragmentDirections.actionNavigationSettingsToPhoneUploadFragment()
            findNavController().navigate(action)
        }

        binding.settingServiceTv.setOnClickListener {
            val action = SettingsFragmentDirections.actionNavigationSettingsToServiceInfoFragment()
            findNavController().navigate(action)
        }

        binding.settingLocationTv.setOnClickListener {
            val action = SettingsFragmentDirections.actionNavigationSettingsToLocationInfoFragment()
            findNavController().navigate(action)
        }

        binding.settingPersonalInfoTv.setOnClickListener {
            val action = SettingsFragmentDirections.actionNavigationSettingsToUserInfoFragment()
            findNavController().navigate(action)
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