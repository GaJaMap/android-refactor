package com.pg.gajamap.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.data.model.GroupEntity
import com.pg.gajamap.data.repository.GroupRepository
import com.pg.gajamap.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val storage: GJMSharedPreference
) : ViewModel() {

    private val _checkResult = MutableSharedFlow<UiState<ArrayList<GroupEntity>>>(replay = 0)
    val checkResult = _checkResult.asSharedFlow()

    private var checkItems = ArrayList<GroupEntity>()

    fun createGroup(name: String) = viewModelScope.launch {
        try {
            _checkResult.emit(UiState.Loading)

            val response = groupRepository.createGroup(name)
            val result = response.body()
            if (response.isSuccessful && result != null) {
                checkItems.add(GroupEntity(result, name, 0))

                _checkResult.emit(UiState.Success(checkItems))
            } else {
                _checkResult.emit(
                    UiState.Failure(
                        "${response.code()}: ${
                            response.errorBody()?.string()
                        }"
                    )
                )
            }
        } catch (e: Exception) {
            _checkResult.emit(UiState.Failure(e.message))
        }
    }

    fun checkGroup() = viewModelScope.launch {
        try {
            _checkResult.emit(UiState.Loading)

            val response = groupRepository.checkGroup(0)
            val result = response.body()
            if (response.isSuccessful && result != null) {
                val totalClientCount = result.groupInfos.sumOf { it.clientCount }

                checkItems.apply {
                    clear()
                    add(GroupEntity(-1, "전체", totalClientCount))
                    addAll(result.groupInfos.map {
                        GroupEntity(it.groupId, it.groupName, it.clientCount)
                    })
                }

                _checkResult.emit(UiState.Success(checkItems))
            } else {
                _checkResult.emit(UiState.Failure("${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            _checkResult.emit(UiState.Failure(e.message))
        }
    }

    fun modifyGroup(groupId: Long, groupName: String) = viewModelScope.launch {
        try {
            _checkResult.emit(UiState.Loading)

            val response = groupRepository.modifyGroup(groupId, groupName)
            if (response.isSuccessful) {
                val modifiedItemIndex = checkItems.indexOfFirst { it.groupId == groupId }
                checkItems[modifiedItemIndex] =
                    checkItems[modifiedItemIndex].copy(groupName = groupName)

                _checkResult.emit(UiState.Success(checkItems))
            } else {
                _checkResult.emit(UiState.Failure("${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            _checkResult.emit(UiState.Failure(e.message))
        }
    }

    fun deleteGroup(groupId: Long) = viewModelScope.launch {
        try {
            _checkResult.emit(UiState.Loading)

            val response = groupRepository.deleteGroup(groupId)
            if (response.isSuccessful) {
                checkItems.removeAll { it.groupId == groupId }

                _checkResult.emit(UiState.Success(checkItems))
            } else {
                _checkResult.emit(UiState.Failure("${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            _checkResult.emit(UiState.Failure(e.message))
        }
    }

    fun saveGroupData(groupId: Long, groupName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                storage.saveGroupId(groupId)
                storage.saveGroupName(groupName)
            }
        }
    }

    fun currentGroupId(): StateFlow<Long> {
        val groupId = runBlocking {
            storage.groupId.first()
        }
        return storage.groupId
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = groupId
            )
    }
}