package com.pg.gajamap.presentation.address

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.internal.ILabel
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelTransition
import com.kakao.vectormap.label.TrackingManager
import com.kakao.vectormap.label.Transition
import com.pg.gajamap.R
import com.pg.gajamap.data.model.response.client.Address
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.data.model.response.client.Location
import com.pg.gajamap.data.model.response.place.Document
import com.pg.gajamap.databinding.FragmentEditAddressBinding
import com.pg.gajamap.presentation.map.PlaceListAdapter
import com.pg.gajamap.util.UiState
import com.pg.gajamap.util.collectLatestStateFlow
import com.pg.gajamap.util.hide
import com.pg.gajamap.util.show
import com.pg.gajamap.util.toast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddressEditFragment : Fragment() {
    private var _binding: FragmentEditAddressBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<AddressEditFragmentArgs>()
    private val addressViewModel: AddressViewModel by viewModels()
    private lateinit var placeListAdapter: PlaceListAdapter

    private lateinit var kakaoMap: KakaoMap

    private lateinit var placeList: List<Document>
    private val placeLabels = mutableListOf<Label>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMapView()
        initSearchView()

        binding.tvLocationBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressedCallback
        )
    }

    private fun initMapView() {
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(error: Exception) {
                error.message?.let { requireContext().toast(it) }
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                this@AddressEditFragment.kakaoMap = kakaoMap
                val client = args.client

                setupRecyclerView(kakaoMap, client)
                observer(kakaoMap)
                initCheckView()
                removePlaceMarkersToMap(kakaoMap)

                if (client.location.latitude != null && client.location.longitude != null) {
                    val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                        LatLng.from(
                            client.location.latitude,
                            client.location.longitude
                        )
                    )
                    kakaoMap.moveCamera(cameraUpdate)
                }

                kakaoMap.setOnCameraMoveEndListener { _, it, _ ->
                    getAddress(
                        it.position.latitude,
                        it.position.longitude
                    )
                }

                binding.tvLocationBtn.setOnClickListener {
                    val action =
                        AddressEditFragmentDirections.actionAddressEditFragmentToEditDetailFragment(
                            Client(
                                Address(
                                    client.address.detail,
                                    binding.tvLocationAddress.text.toString()
                                ),
                                client.clientId,
                                client.clientName,
                                client.createdAt,
                                client.distance,
                                client.groupInfo,
                                client.image,
                                Location(
                                    addressViewModel.currentLocation.value?.first,
                                    addressViewModel.currentLocation.value?.second
                                ),
                                client.phoneNumber
                            )
                        )
                    findNavController().navigate(action)
                }
            }
        })
    }

    private fun initCheckView() {
        binding.ibGps.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestPermission {
                    addressViewModel.setGpsLocation()
                }
            } else {
                kakaoMap.trackingManager!!.stopTracking()
            }
        }
    }

    private fun initSearchView() {
        binding.etLocationSearch.isSubmitButtonEnabled = true
        binding.etLocationSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                addressViewModel.getPlace(query)
                binding.clLocationSearch.visibility = View.VISIBLE
                binding.clLocation.visibility = View.GONE
                binding.ibGps.visibility = View.GONE
                binding.icMapPin.visibility = View.GONE
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun observer(kakaoMap: KakaoMap) {
        collectLatestStateFlow(addressViewModel.addressResult) {
            when (it) {
                is UiState.Loading -> {
                    binding.tvLocationAddress.text = "내 위치 검색중..."
                    binding.tvLocationBtn.isEnabled = false
                }

                is UiState.Success -> {
                    val address = it.data.documents.getOrNull(0)?.roadAddress?.addressName
                        ?: it.data.documents.getOrNull(0)?.address?.addressName

                    binding.tvLocationAddress.text = address
                    binding.tvLocationBtn.isEnabled = !address.isNullOrEmpty()
                }

                is UiState.Failure -> {
                    requireContext().toast(it.error!!)
                    binding.tvLocationBtn.isEnabled = false
                }
            }
        }

        collectLatestStateFlow(addressViewModel.placeResult) { result ->
            result?.let {
                when (it) {
                    is UiState.Loading -> {
                        placeListAdapter.submitList(null)
                        removePlaceMarkersToMap(kakaoMap)

                        binding.progress.show(requireActivity())
                    }

                    is UiState.Success -> {
                        placeList = it.data.documents
                        placeListAdapter.submitData(placeList)
                        addPlaceMarkersToMap(kakaoMap, placeList)

                        binding.progress.hide(requireActivity())
                    }

                    is UiState.Failure -> {
                        requireContext().toast(it.error!!)
                        binding.progress.hide(requireActivity())
                    }
                }
            }
        }

        collectLatestStateFlow(addressViewModel.gpsLocation) { location ->
            when {
                binding.ibGps.isChecked -> startTracking(kakaoMap, location.first, location.second)
            }
        }
    }

    private fun setupRecyclerView(kakaoMap: KakaoMap, client: Client) {
        placeListAdapter = PlaceListAdapter()
        binding.rvLocation.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = placeListAdapter
        }
        placeListAdapter.setOnItemClickListener {
            val cameraUpdate =
                CameraUpdateFactory.newCenterPosition(LatLng.from(it.y.toDouble(), it.x.toDouble()))
            kakaoMap.moveCamera(cameraUpdate, CameraAnimation.from(500, true, true))
            getAddress(it.y.toDouble(), it.x.toDouble())
        }
        placeListAdapter.setOnAddClickListener {
            val address = it.roadAddressName?.ifBlank { it.addressName }
            val action =
                AddressEditFragmentDirections.actionAddressEditFragmentToEditDetailFragment(
                    Client(
                        Address(
                            client.address.detail,
                            address
                        ),
                        client.clientId,
                        client.clientName,
                        client.createdAt,
                        client.distance,
                        client.groupInfo,
                        client.image,
                        Location(
                            it.y.toDouble(),
                            it.x.toDouble()
                        ),
                        client.phoneNumber
                    )
                )
            findNavController().navigate(action)
        }
    }

    private fun addPlaceMarkersToMap(kakaoMap: KakaoMap, places: List<Document>?) {
        val style = LabelStyle.from(R.drawable.png_map_pin)
            .setIconTransition(LabelTransition.from(Transition.Scale, Transition.Scale))

        places?.forEach { place ->
            val options = LabelOptions.from(LatLng.from(place.y.toDouble(), place.x.toDouble()))
                .setStyles(style)

            val label = kakaoMap.labelManager?.layer?.addLabel(options)
            placeLabels.add(label!!)
        }
    }

    private fun removePlaceMarkersToMap(kakaoMap: KakaoMap) {
        kakaoMap.labelManager?.layer?.remove(placeLabels as Collection<ILabel>?)
        placeLabels.clear()
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        addressViewModel.updateLocation(latitude, longitude)
        addressViewModel.getAddress(longitude.toString(), latitude.toString())
    }

    private fun requestPermission(logic: () -> Unit) {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    logic()
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    requireContext().toast("권한 거부\n$deniedPermissions")
                    binding.ibGps.isChecked = false
                }
            })
            .setDeniedMessage("권한을 허용해주세요. [설정] > [앱 및 알림] > [고급] > [앱 권한]")
            .setPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .check()
    }

    private fun startTracking(kakaoMap: KakaoMap, latitude: Double, longitude: Double) {
        val style: LabelStyle = LabelStyle.from(R.drawable.ic_free_profile)
        val options = LabelOptions.from(LatLng.from(latitude, longitude)).setStyles(style)

        val layer = kakaoMap.labelManager!!.layer?.addLabel(options)
        val trackingManager: TrackingManager = kakaoMap.trackingManager!!
        trackingManager.startTracking(layer)
        trackingManager.setTrackingRotation(true)
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.clLocationSearch.isVisible) {
                    binding.clLocationSearch.visibility = View.GONE
                    binding.clLocation.visibility = View.VISIBLE
                    binding.ibGps.visibility = View.VISIBLE
                    binding.icMapPin.visibility = View.VISIBLE
                    removePlaceMarkersToMap(kakaoMap)
                } else {
                    this.isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}