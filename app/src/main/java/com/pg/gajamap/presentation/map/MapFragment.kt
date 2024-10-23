package com.pg.gajamap.presentation.map

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.label.LabelTransition
import com.kakao.vectormap.label.TrackingManager
import com.kakao.vectormap.label.Transition
import com.kakao.vectormap.shape.DotPoints
import com.kakao.vectormap.shape.PolygonOptions
import com.pg.gajamap.R
import com.pg.gajamap.data.model.AddressInfo
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.data.model.response.place.Document
import com.pg.gajamap.databinding.FragmentMapBinding
import com.pg.gajamap.util.UiState
import com.pg.gajamap.util.collectLatestStateFlow
import com.pg.gajamap.util.collectLatestStateFlowWithDrop
import com.pg.gajamap.util.getTextChangeStateFlow
import com.pg.gajamap.util.hide
import com.pg.gajamap.util.show
import com.pg.gajamap.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val mapViewModel: MapViewModel by viewModels()

    private lateinit var searchListAdapter: SearchListAdapter
    private lateinit var placeListAdapter: PlaceListAdapter

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var clientList: List<Client>
    private val clientLabels = mutableListOf<Label>()

    private lateinit var placeList: List<Document>
    private val placeLabels = mutableListOf<Label>()

    private var trackingLabel: Label? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMapView()
        initSearchView()
        initClickView()
        setupBackPressedCallback()
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
                setupRecyclerView(kakaoMap)
                observer(kakaoMap)
//                moveCameraToInitialLocation(kakaoMap)
//                setOnCameraMoveEndListener(kakaoMap)
                initCheckView(kakaoMap)
                setOnLabelClickListener(kakaoMap)
            }
        })
    }

    private fun initClickView() {
        binding.tvLocationBtn.setOnClickListener {
            val action = MapFragmentDirections.actionNavigationMapToAddCustomerFragment(
                AddressInfo(
                    binding.tvLocationAddress.text.toString(),
                    mapViewModel.currentLocation.value?.first,
                    mapViewModel.currentLocation.value?.second,
                    mapViewModel.groupState.replayCache[0]
                )
            )
            findNavController().navigate(action)
        }

        binding.clSearch.setOnClickListener {
            val action = MapFragmentDirections.actionNavigationMapToGroupSheetFragment()
            findNavController().navigate(action)
        }
    }

    private fun initSearchView() {
        binding.etLocationSearch.isSubmitButtonEnabled = true
        binding.etLocationSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mapViewModel.getPlace(query)
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

    private fun initCheckView(kakaoMap: KakaoMap) {
        binding.ibGps.setOnCheckedChangeListener { _, isChecked ->
            mapViewModel.setGpsChecked(isChecked)
        }

        binding.ibPlus.setOnCheckedChangeListener { _, isChecked ->
            mapViewModel.setPlusChecked(isChecked)
        }

        binding.ibKm.setOnCheckedChangeListener { _, isChecked ->
            when {
                isChecked -> {
                    requestPermission {
                        if (binding.btn3km.isChecked || binding.btn5km.isChecked) {
                            binding.ibKm.isChecked = false
                            binding.clKm.visibility = View.GONE
                        } else {
                            binding.clKm.visibility = View.VISIBLE
                        }
                    }
                }

                (binding.btn3km.isChecked || binding.btn5km.isChecked) && !binding.clKm.isVisible -> {
                    binding.ibKm.isChecked = true
                    binding.clKm.visibility = View.VISIBLE
                }

                (binding.btn3km.isChecked || binding.btn5km.isChecked) && binding.clKm.isVisible -> {
                    binding.ibKm.isChecked = true
                    binding.clKm.visibility = View.GONE
                }

                else -> {
                    binding.clKm.visibility = View.GONE
                    mapViewModel.setRadius(0)
                }
            }
        }

        binding.btn3km.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mapViewModel.setGpsLocation()

                binding.btn5km.isChecked = false
                binding.clKm.visibility = View.GONE
            } else {
                binding.clKm.visibility = View.GONE
                binding.ibKm.isChecked = false

                kakaoMap.shapeManager?.layer?.removeAll()
            }
        }

        binding.btn5km.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mapViewModel.setGpsLocation()

                binding.btn3km.isChecked = false
                binding.clKm.visibility = View.GONE
            } else {
                binding.clKm.visibility = View.GONE
                binding.ibKm.isChecked = false

                kakaoMap.shapeManager?.layer?.removeAll()
            }
        }
    }

    private fun moveCameraToInitialLocation(kakaoMap: KakaoMap) {
        lifecycleScope.launch {
            val cameraUpdate =
                CameraUpdateFactory.newCenterPosition(
                    LatLng.from(
                        mapViewModel.getLocation().first!!,
                        mapViewModel.getLocation().second!!
                    )
                )

            kakaoMap.moveCamera(cameraUpdate)
        }
    }

    private fun setOnLabelClickListener(kakaoMap: KakaoMap) {
        kakaoMap.setOnLabelClickListener { _, _, label ->
            val clientInfo =
                clientList.find { it.location.latitude == label?.position?.latitude && it.location.longitude == label?.position?.longitude }
            mapViewModel.setClientInfo(clientInfo)
        }
    }

    private fun setOnCameraMoveEndListener(kakaoMap: KakaoMap) {
        kakaoMap.setOnCameraMoveEndListener { _, it, _ ->
            if (binding.ibPlus.isChecked) {
                getAddress(it.position.latitude, it.position.longitude)
            }
            mapViewModel.saveCurrentLocation(it.position.latitude, it.position.longitude)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observer(kakaoMap: KakaoMap) {
        collectLatestStateFlow(mapViewModel.groupState) { groupState ->
            binding.tvSearch.text = groupState.groupName
            mapViewModel.getClient(groupState.groupId, "")
        }

        collectLatestStateFlow(
            combine(
                mapViewModel.groupState,
                mapViewModel.radiusResult,
            ) { group, radius ->
                Pair(group.groupId, radius)
            }
                .flatMapLatest { (groupId, radius) ->
                    if (radius == 0) {
                        mapViewModel.getClient(groupId, "")
                    } else {
                        mapViewModel.getRadiusClient(
                            groupId,
                            radius,
                            mapViewModel.currentLocation.value!!.first,
                            mapViewModel.currentLocation.value!!.second
                        )
                    }
                }
        ) { uiState ->
            when (uiState) {
                is UiState.Loading -> {}
                is UiState.Success -> {

                    clientList = uiState.data.clients
                    removePlaceMarkersToMap(kakaoMap)
                    removeClientMarkersToMap(kakaoMap)
                    addClientMarkersToMap(kakaoMap, clientList)
                }

                is UiState.Failure -> requireContext().toast(uiState.error!!)
            }
        }

        collectLatestStateFlow(binding.etSearch.getTextChangeStateFlow()) { searchText ->
            if (searchText.isNotEmpty()) {
                val filteredClients = clientList.filter { client ->
                    !client.address.mainAddress.isNullOrEmpty() && client.clientName.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                searchListAdapter.submitList(filteredClients)
            }
        }

        collectLatestStateFlow(mapViewModel.addressResult) {
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

        collectLatestStateFlow(mapViewModel.placeResult) { result ->
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

        collectLatestStateFlow(mapViewModel.gpsLocation) { location ->
            when {
                binding.btn3km.isChecked -> {
                    mapViewModel.setRadius(3000)
                    cameraUpdate(kakaoMap, location.first, location.second)
                    addCirclePolygon(kakaoMap, location, 3000f)
                }

                binding.btn5km.isChecked -> {
                    mapViewModel.setRadius(5000)
                    cameraUpdate(kakaoMap, location.first, location.second)
                    addCirclePolygon(kakaoMap, location, 5000f)
                }
            }
        }

        collectLatestStateFlow(mapViewModel.trackerResult) { location ->
            kakaoMap.labelManager?.layer?.remove(trackingLabel)
            startTracking(kakaoMap, location.first, location.second)
        }

        collectLatestStateFlow(mapViewModel.clientResult) { client ->
            if (client != null) {
                viewPagerAdapter = ViewPagerAdapter(client)
                binding.vpClient.adapter = viewPagerAdapter

                showCardView()
            }
        }

        collectLatestStateFlowWithDrop(mapViewModel.gpsChecked) { isChecked ->
            if (isChecked) {
                requestPermission {
                    mapViewModel.startTracker()
                }
            } else {
                mapViewModel.stopTracker()
                kakaoMap.trackingManager!!.stopTracking()
                kakaoMap.labelManager?.layer?.remove(trackingLabel)
            }
        }

        collectLatestStateFlowWithDrop(mapViewModel.plusChecked) { isChecked ->
            if (isChecked) {
                binding.clKm.visibility = View.GONE
                binding.ibPlus.visibility = View.GONE
                binding.ibKm.visibility = View.GONE
                binding.clLocation.visibility = View.VISIBLE
                binding.clSearchWhole.visibility = View.GONE
                binding.clSearchLocation.visibility = View.VISIBLE
                binding.icMapPin.visibility = View.VISIBLE
                binding.rvSearch.visibility = View.GONE

                removeClientMarkersToMap(kakaoMap)

                getAddress(
                    kakaoMap.cameraPosition?.position?.latitude!!,
                    kakaoMap.cameraPosition?.position?.longitude!!
                )
            } else {
                binding.ibPlus.visibility = View.VISIBLE
                binding.ibKm.visibility = View.VISIBLE
                binding.ibGps.visibility = View.VISIBLE
                binding.clLocation.visibility = View.GONE
                binding.clSearchWhole.visibility = View.VISIBLE
                binding.clSearchLocation.visibility = View.GONE
                binding.clLocationSearch.visibility = View.GONE
                binding.icMapPin.visibility = View.GONE
                binding.rvSearch.visibility = View.VISIBLE

                removePlaceMarkersToMap(kakaoMap)
                addClientMarkersToMap(kakaoMap, clientList)
            }
        }
    }

    private fun setupRecyclerView(kakaoMap: KakaoMap) {
        searchListAdapter = SearchListAdapter()
        binding.rvSearch.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = searchListAdapter
        }
        searchListAdapter.setOnItemClickListener { search ->
            val clientInfo = clientList.find { it.clientId == search.clientId }
            mapViewModel.setClientInfo(clientInfo)

            cameraUpdate(kakaoMap, search.location.latitude!!, search.location.longitude!!)
        }

        placeListAdapter = PlaceListAdapter()
        binding.rvLocation.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = placeListAdapter
        }
        placeListAdapter.setOnItemClickListener {
            cameraUpdate(kakaoMap, it.y.toDouble(), it.x.toDouble())
            getAddress(it.y.toDouble(), it.x.toDouble())
        }
        placeListAdapter.setOnAddClickListener {
            val address = it.roadAddressName?.ifBlank { it.addressName }
            val action =
                MapFragmentDirections.actionNavigationMapToAddCustomerFragment(
                    AddressInfo(
                        address!!,
                        it.y.toDouble(),
                        it.x.toDouble(),
                        mapViewModel.groupState.replayCache[0]
                    )
                )
            findNavController().navigate(action)
        }
    }

    private fun cameraUpdate(kakaoMap: KakaoMap, latitude: Double, longitude: Double) {
        val cameraUpdate = CameraUpdateFactory.newCenterPosition(LatLng.from(latitude, longitude))
        kakaoMap.moveCamera(cameraUpdate, CameraAnimation.from(500, true, true))
    }

    private fun addClientMarkersToMap(kakaoMap: KakaoMap, clients: List<Client>?) {
        val styles = LabelStyles.from(
            LabelStyle.from(R.drawable.png_map_pin)
                .setIconTransition(LabelTransition.from(Transition.Scale, Transition.Scale))
                .setZoomLevel(5),
            LabelStyle.from(R.drawable.png_map_pin)
                .setTextStyles(LabelTextStyle.from(28, R.color.black))
                .setIconTransition(LabelTransition.from(Transition.Scale, Transition.Scale))
                .setZoomLevel(10)
        )

        clients?.forEach { client ->
            if (client.location.latitude != null && client.location.longitude != null) {
                val options = LabelOptions.from(
                    LatLng.from(client.location.latitude, client.location.longitude)
                )
                    .setStyles(styles)
                    .setTexts(client.clientName)
                val label = kakaoMap.labelManager?.layer?.addLabel(options)
                clientLabels.add(label!!)
            }
        }
    }

    private fun removeClientMarkersToMap(kakaoMap: KakaoMap) {
        kakaoMap.labelManager?.layer?.remove(clientLabels as Collection<ILabel>?)
        clientLabels.clear()
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

    private fun addCirclePolygon(
        kakaoMap: KakaoMap,
        location: Pair<Double, Double>,
        radius: Float
    ) {
        val circleOptions: PolygonOptions = PolygonOptions.from(
            DotPoints.fromCircle(LatLng.from(location.first, location.second), radius),
            Color.parseColor("#80ff2c35")
        )

        kakaoMap.shapeManager?.layer!!.addPolygon(circleOptions)
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        mapViewModel.setCurrentLocation(latitude, longitude)
        mapViewModel.getAddress(longitude.toString(), latitude.toString())
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
                    binding.ibKm.isChecked = false
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
        val style: LabelStyle = LabelStyle.from(R.drawable.ic_direction_area)
        val options = LabelOptions.from(LatLng.from(latitude, longitude)).setStyles(style)

        trackingLabel = kakaoMap.labelManager?.layer?.addLabel(options)
        val trackingManager: TrackingManager = kakaoMap.trackingManager!!

        trackingManager.startTracking(trackingLabel)
    }

    private fun setupBackPressedCallback() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    binding.ibPlus.isChecked -> {
                        binding.ibPlus.isChecked = false
                    }

                    binding.clCardview.isVisible -> {
                        hideCardView()
                    }

                    else -> {
                        this.isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun showCardView() {
        binding.clCardview.visibility = View.VISIBLE
        binding.ibGps.visibility = View.GONE
        binding.ibPlus.visibility = View.GONE
        binding.ibKm.visibility = View.GONE
        binding.clKm.visibility = View.GONE
//        downKeyboard(requireContext(), binding.etSearch)
    }

    private fun hideCardView() {
        binding.clCardview.visibility = View.GONE
        binding.ibPlus.visibility = View.VISIBLE
        binding.ibKm.visibility = View.VISIBLE
        binding.ibGps.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        binding.ibPlus.isChecked = false
        binding.ibKm.isChecked = false
        binding.ibGps.isChecked = false
        binding.btn3km.isChecked = false
        binding.btn5km.isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}