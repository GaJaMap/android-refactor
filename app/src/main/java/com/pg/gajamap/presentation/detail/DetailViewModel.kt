package com.pg.gajamap.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pg.gajamap.data.model.request.MemoRequest
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.data.model.response.memo.Memo
import com.pg.gajamap.data.repository.ClientRepository
import com.pg.gajamap.data.repository.MemoRepository
import com.pg.gajamap.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val memoRepository: MemoRepository
) : ViewModel() {

    private val _deleteResult = MutableStateFlow<UiState<Unit>?>(null)
    val deleteResult = _deleteResult.asStateFlow()

    private val _createMemoResult = MutableStateFlow<UiState<Unit>?>(null)
    val createMemoResult = _createMemoResult.asStateFlow()

    private val _deleteMemoResult = MutableStateFlow<UiState<Unit>?>(null)
    val deleteMemoResult = _deleteMemoResult.asStateFlow()

    private val _profileNameFlow = MutableStateFlow("")

    private val _profilePhoneFlow = MutableStateFlow("")

    private val _memosPaging = MutableStateFlow<PagingData<Memo>>(PagingData.empty())
    val memosPaging = _memosPaging.asStateFlow()

    fun deleteClient(groupId: Long, clientId: Long) = viewModelScope.launch {
        try {
            _deleteResult.emit(UiState.Loading)

            val response = clientRepository.deleteClient(groupId, clientId)
            if (response.isSuccessful) {
                _deleteResult.emit(UiState.Success(Unit))
            } else {
                _deleteResult.emit(UiState.Failure("${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            _deleteResult.emit(UiState.Failure(e.message))
        }
    }

    private val _putResult = MutableStateFlow<UiState<Client>?>(null)
    val putResult = _putResult.asStateFlow()

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

    val isButtonEnabled: Flow<Boolean> = combine(
        _profileNameFlow,
        _profilePhoneFlow,
    ) { profileName, profilePhone ->
        profileName.isNotEmpty() && profilePhone.isNotEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun updateProfileName(name: String) {
        _profileNameFlow.value = name
    }

    fun updateProfilePhone(phone: String) {
        _profilePhoneFlow.value = phone
    }

    fun createMemo(clientId: Long, memoRequest: MemoRequest) = viewModelScope.launch {
        try {
            _createMemoResult.emit(UiState.Loading)

            val response = memoRepository.createMemo(clientId, memoRequest)
            if (response.isSuccessful) {
                _createMemoResult.emit(UiState.Success(Unit))

            } else {
                _createMemoResult.emit(
                    UiState.Failure(
                        "${response.code()}: ${
                            response.errorBody()?.string()
                        }"
                    )
                )
            }
        } catch (e: Exception) {
            _createMemoResult.emit(UiState.Failure(e.message))
        }
    }

    fun deleteMemo(memoId: Int) = viewModelScope.launch {
        try {
            _deleteMemoResult.emit(UiState.Loading)

            val response = memoRepository.deleteMemo(memoId)
            if (response.isSuccessful) {
                _deleteMemoResult.emit(UiState.Success(Unit))

            } else {
                _deleteMemoResult.emit(
                    UiState.Failure(
                        "${response.code()}: ${
                            response.errorBody()?.string()
                        }"
                    )
                )
            }
        } catch (e: Exception) {
            _deleteMemoResult.emit(UiState.Failure(e.message))
        }
    }

    fun getMemosPaging(clientId: Long) {
        viewModelScope.launch {
            memoRepository.getMemosPaging(clientId)
                .cachedIn(viewModelScope)
                .collect {
                    _memosPaging.value = it
                }
        }
    }
}