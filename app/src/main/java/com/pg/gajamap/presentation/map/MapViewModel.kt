package com.pg.gajamap.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.data.manager.LocationTracker
import com.pg.gajamap.data.model.GroupState
import com.pg.gajamap.data.model.response.address.AddressResponse
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.data.model.response.client.ClientResponse
import com.pg.gajamap.data.model.response.place.PlaceResponse
import com.pg.gajamap.data.repository.ClientRepository
import com.pg.gajamap.data.repository.KakaoRepository
import com.pg.gajamap.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val kakaoRepository: KakaoRepository,
    private val locationTracker: LocationTracker,
    private val storage: GJMSharedPreference
) : ViewModel() {

    private val _addressResult = MutableStateFlow<UiState<AddressResponse>>(UiState.Loading)
    val addressResult = _addressResult.asStateFlow()

    private val _placeResult = MutableStateFlow<UiState<PlaceResponse>?>(null)
    val placeResult = _placeResult.asStateFlow()

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _gpsLocation = MutableSharedFlow<Pair<Double, Double>>(replay = 0)
    val gpsLocation = _gpsLocation.asSharedFlow()

    private val _trackerResult = MutableSharedFlow<Pair<Double, Double>>(replay = 0)
    val trackerResult = _trackerResult.asSharedFlow()

    private val _radiusResult = MutableStateFlow(0)
    val radiusResult = _radiusResult.asStateFlow()

    private val _clientResult = MutableSharedFlow<Client?>(replay = 0)
    val clientResult = _clientResult.asSharedFlow()

    private val _gpsChecked = MutableStateFlow(false)
    val gpsChecked = _gpsChecked.asStateFlow()

    private val _plusChecked = MutableStateFlow(false)
    val plusChecked = _plusChecked.asStateFlow()

    private val _kmChecked = MutableStateFlow(false)
    val kmChecked = _kmChecked.asStateFlow()

    private val _btn3kmChecked = MutableStateFlow(false)
    val btn3kmChecked = _btn3kmChecked.asStateFlow()

    private val _btn5kmChecked = MutableStateFlow(false)
    val btn5kmChecked = _btn5kmChecked.asStateFlow()

    fun getClient(groupId: Long = -1L, wordCond: String): Flow<UiState<ClientResponse>> = flow {
        try {
            emit(UiState.Loading)

            val response = if (groupId == -1L) {
                clientRepository.getAllClient(wordCond)
            } else {
                clientRepository.getGroupClient(groupId, wordCond)
            }
            val result = response.body()
            if (response.isSuccessful && result != null) {
                emit(UiState.Success(result))
            } else {
                emit(UiState.Failure("${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(UiState.Failure(e.message))
        }
    }

    fun getRadiusClient(
        groupId: Long = -1L,
        radius: Int,
        latitude: Double,
        longitude: Double
    ): Flow<UiState<ClientResponse>> = flow {
        try {
            emit(UiState.Loading)

            val response = if (groupId == -1L) {
                clientRepository.wholeRadius(radius, latitude, longitude)
            } else {
                clientRepository.specificRadius(groupId, radius, latitude, longitude)
            }
            val result = response.body()
            if (response.isSuccessful && result != null) {
                emit(UiState.Success(result))
            } else {
                emit(UiState.Failure("${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(UiState.Failure(e.message))
        }
    }

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

    val groupState = combine(
        storage.groupId,
        storage.groupName
    ) { groupId, groupName -> GroupState(groupId, groupName) }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    fun setCurrentLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _currentLocation.emit(Pair(latitude, longitude))
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

    fun startTracker() {
        locationTracker.startTracker {
            viewModelScope.launch {
                _trackerResult.emit(Pair(it.latitude, it.longitude))
                _currentLocation.emit(Pair(it.latitude, it.longitude))
                getAddress(it.longitude.toString(), it.latitude.toString())
            }
        }
    }

    fun stopTracker() {
        locationTracker.stopTracker()
    }

    fun setRadius(radius: Int) {
        viewModelScope.launch {
            _radiusResult.emit(radius)
        }
    }

    fun setClientInfo(client: Client?) {
        viewModelScope.launch {
            _clientResult.emit(client)
        }
    }

    suspend fun getLocation() = withContext(Dispatchers.IO) {
        storage.location.first()
    }

    fun saveCurrentLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            storage.saveLocation(latitude, longitude)
        }
    }

    fun setGpsChecked(isChecked: Boolean) {
        _gpsChecked.value = isChecked
    }

    fun setPlusChecked(isChecked: Boolean) {
        _plusChecked.value = isChecked
    }

    fun setKmChecked(isChecked: Boolean) {
        _kmChecked.value = isChecked
    }

    fun setBtn3kmChecked(isChecked: Boolean) {
        _btn3kmChecked.value = isChecked
    }

    fun setBtn5kmChecked(isChecked: Boolean) {
        _btn5kmChecked.value = isChecked
    }
}