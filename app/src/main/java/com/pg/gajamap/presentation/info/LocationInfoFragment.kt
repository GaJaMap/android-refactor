package com.pg.gajamap.presentation.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pg.gajamap.databinding.FragmentLocationInfoBinding
import com.pg.gajamap.util.getAssetsTextString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationInfoFragment : Fragment() {
    private var _binding: FragmentLocationInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.locationInfoContent.text =
            requireContext().getAssetsTextString("LocationInfoContent")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}