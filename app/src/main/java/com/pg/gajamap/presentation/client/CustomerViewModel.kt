package com.pg.gajamap.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.data.model.GroupState
import com.pg.gajamap.data.model.request.DeleteRequest
import com.pg.gajamap.data.model.request.MemoRequest
import com.pg.gajamap.data.model.response.client.ClientResponse
import com.pg.gajamap.data.repository.ClientRepository
import com.pg.gajamap.data.repository.MemoRepository
import com.pg.gajamap.util.SortType
import com.pg.gajamap.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val memoRepository: MemoRepository,
    private val storage: GJMSharedPreference
) : ViewModel() {

    private val _deleteResult = MutableStateFlow<UiState<Unit>?>(null)
    val deleteResult = _deleteResult.asStateFlow()

    private val _createMemo = MutableStateFlow<UiState<Unit>?>(null)
    val createMemo = _createMemo.asStateFlow()

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

    val groupState = combine(
        storage.groupId,
        storage.groupName
    ) { groupId, groupName -> GroupState(groupId, groupName) }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    val currentSortType: StateFlow<SortType> = storage.sortType
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SortType.NEWEST
        )

    fun saveSortType(sortType: SortType) {
        viewModelScope.launch {
            storage.saveSortType(sortType)
        }
    }

    fun deleteAnyClient(groupId: Long, request: DeleteRequest) = viewModelScope.launch {
        try {
            _deleteResult.emit(UiState.Loading)

            val response = clientRepository.deleteAnyClient(groupId, request)
            if (response.isSuccessful) {
                _deleteResult.emit(UiState.Success(Unit))
            } else {
                _deleteResult.emit(UiState.Failure("${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            _deleteResult.emit(UiState.Failure(e.message))
        }
    }

    fun createMemo(clientId: Long, memoRequest: MemoRequest) = viewModelScope.launch {
        try {
            _createMemo.emit(UiState.Loading)

            val response = memoRepository.createMemo(clientId, memoRequest)
            if (response.isSuccessful) {
                _createMemo.emit(UiState.Success(Unit))

            } else {
                _createMemo.emit(
                    UiState.Failure(
                        "${response.code()}: ${
                            response.errorBody()?.string()
                        }"
                    )
                )
            }
        } catch (e: Exception) {
            _createMemo.emit(UiState.Failure(e.message))
        }
    }
}