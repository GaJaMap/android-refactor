package com.pg.gajamap.presentation.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pg.gajamap.databinding.FragmentServiceInfoBinding
import com.pg.gajamap.util.getAssetsTextString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServiceInfoFragment : Fragment() {
    private var _binding: FragmentServiceInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.serviceInfoContent.text =
            requireContext().getAssetsTextString("ServiceInfoContent")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}