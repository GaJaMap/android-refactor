package com.pg.gajamap.data.repository

import com.pg.gajamap.data.model.response.group.GroupResponse
import com.pg.gajamap.data.service.GroupService
import retrofit2.Response
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val service: GroupService
) : GroupRepository {

    override suspend fun createGroup(name: String): Response<Long> {
        return service.createGroup(name)
    }

    override suspend fun checkGroup(page: Int): Response<GroupResponse> {
        return service.checkGroup(page)
    }

    override suspend fun deleteGroup(groupId: Long): Response<Void> {
        return service.deleteGroup(groupId)
    }

    override suspend fun modifyGroup(groupId: Long, name: String): Response<Void> {
        return service.modifyGroup(groupId, name)
    }

}