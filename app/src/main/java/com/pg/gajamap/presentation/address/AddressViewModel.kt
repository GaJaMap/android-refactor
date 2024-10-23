package com.pg.gajamap.presentation.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.data.manager.LocationTracker
import com.pg.gajamap.data.model.GroupState
import com.pg.gajamap.data.model.response.address.AddressResponse
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.data.model.response.place.PlaceResponse
import com.pg.gajamap.data.repository.ClientRepository
import com.pg.gajamap.data.repository.KakaoRepository
import com.pg.gajamap.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val kakaoRepository: KakaoRepository,
    private val storage: GJMSharedPreference,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _addressResult = MutableStateFlow<UiState<AddressResponse>>(UiState.Loading)
    val addressResult = _addressResult.asStateFlow()

    private val _placeResult = MutableStateFlow<UiState<PlaceResponse>?>(null)
    val placeResult = _placeResult.asStateFlow()

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _putResult = MutableStateFlow<UiState<Client>?>(null)
    val putResult = _putResult.asStateFlow()

    private val _gpsLocation = MutableSharedFlow<Pair<Double, Double>>(replay = 0)
    val gpsLocation = _gpsLocation.asSharedFlow()

    fun getAddress(x: String, y: String) = viewModelScope.launch {
        try {
            val response = kakaoRepository.getAddress(x, y)
            val result = response.body()
            if (response.isSuccessful && result != null) {
                _addressResult.value = UiState.Success(result)
            } else {
                _addressResult.value = UiState.Failure("${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            _addressResult.value = UiState.Failure(e.message)
        }
    }

    fun getPlace(query: String) = viewModelScope.launch {
        try {
            _placeResult.value = UiState.Loading

            val response = kakaoRepository.getPlace(query, 1)
            val result = response.body()
            if (response.isSuccessful && result != null) {
                _placeResult.value = UiState.Success(result)
            } else {
                _placeResult.value =
                    UiState.Failure("${response.code()}: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            _placeResult.value = UiState.Failure(e.message)
        }
    }

//    val groupState = combine(
//        storage.groupId,
//        storage.groupName
//    ) { groupId, groupName -> GroupState(groupId, groupName) }
//        .shareIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            replay = 1
//        )

    fun groupState(): StateFlow<GroupState> {
        val groupState = runBlocking {
            GroupState(storage.groupId.first(), storage.groupName.first())
        }
        return combine(storage.groupId, storage.groupName) { groupId, groupName ->
            GroupState(groupId, groupName)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = groupState
            )
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _currentLocation.value = Pair(latitude, longitude)
    }

    fun putClient(
        groupIdPath: Long,
        clientId: Long,
        clientName: RequestBody,
        groupIdPart: RequestBody,
        phoneNumber: RequestBody,
        mainAddress: RequestBody,
        detail: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        clientImage: MultipartBody.Part?,
        isBasicImage: RequestBody
    ) = viewModelScope.launch {
        _putResult.value = UiState.Loading

        try {
            val response = clientRepository.putClient(
                groupIdPath,
                clientId,
                clientName,
                groupIdPart,
                phoneNumber,
                mainAddress,
                detail,
                latitude,
                longitude,
                clientImage,
                isBasicImage
            )
            val result = response.body()
            if (response.isSuccessful && result != null) {
                _putResult.value = UiState.Success(result)
            } else {
                _putResult.value = UiState.Failure("${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            _putResult.value = UiState.Failure(e.message)
        }
    }

    fun setGpsLocation() {
        viewModelScope.launch {
            locationTracker.getCurrentLocation()?.let {
                _gpsLocation.emit(Pair(it.latitude, it.longitude))
                _currentLocation.emit(Pair(it.latitude, it.longitude))
                getAddress(it.longitude.toString(), it.latitude.toString())
            }
        }
    }

}