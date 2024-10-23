package com.pg.gajamap.presentation.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.data.model.GroupEntity
import com.pg.gajamap.data.model.GroupState
import com.pg.gajamap.data.model.request.upload.Client
import com.pg.gajamap.data.model.request.upload.UploadRequest
import com.pg.gajamap.data.repository.ClientRepository
import com.pg.gajamap.data.repository.GroupRepository
import com.pg.gajamap.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val clientRepository: ClientRepository,
    private val storage: GJMSharedPreference
) : ViewModel() {

    private val _checkResult = MutableStateFlow<UiState<ArrayList<GroupEntity>>>(UiState.Loading)
    val checkResult = _checkResult.asStateFlow()

    private val _checkItems = ArrayList<GroupEntity>()
    val checkItems: List<GroupEntity> get() = _checkItems

    private val _postResult = MutableStateFlow<UiState<List<Int>>?>(null)
    val postResult = _postResult.asStateFlow()

    private val _phoneListFlow = MutableStateFlow(false)

    private val _groupIdFlow = MutableStateFlow(-1L)

    fun checkGroup() = viewModelScope.launch {
        try {
            val response = groupRepository.checkGroup(0)
            val result = response.body()
            if (response.isSuccessful && result != null) {

                _checkItems.apply {
                    clear()
                    addAll(result.groupInfos.map {
                        GroupEntity(it.groupId, it.groupName, it.clientCount)
                    })
                }

                _checkResult.emit(UiState.Success(_checkItems))
            } else {
                _checkResult.emit(UiState.Failure("${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            _checkResult.emit(UiState.Failure(e.message))
        }
    }

    fun postUploadClient(
        client: MutableList<Client>
    ) = viewModelScope.launch {
        _postResult.value = UiState.Loading

        try {
            val response =
                clientRepository.postUploadClient(UploadRequest(client, _groupIdFlow.first()))
            val result = response.body()
            if (response.isSuccessful && result != null) {
                _postResult.value = UiState.Success(result)
            } else {
                _postResult.value = UiState.Failure("${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            _postResult.value = UiState.Failure(e.message)
        }
    }

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

    val isButtonEnabled: Flow<Boolean> = combine(
        _phoneListFlow,
        _groupIdFlow
    ) { phoneList, groupId ->
        phoneList && groupId != -1L
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun updateIsChecked(phone: Boolean) {
        _phoneListFlow.value = phone
    }

    fun updateGroupId(pos: Int) {
        val groupId = if (pos != 0) {
            checkItems[pos - 1].groupId
        } else {
            -1L
        }
        _groupIdFlow.value = groupId
    }
}