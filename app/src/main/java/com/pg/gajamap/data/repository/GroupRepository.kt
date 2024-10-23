package com.pg.gajamap.data.repository

import com.pg.gajamap.data.model.response.group.GroupResponse
import retrofit2.Response

interface GroupRepository {

    suspend fun createGroup(name: String): Response<Long>

    suspend fun checkGroup(page: Int): Response<GroupResponse>

    suspend fun deleteGroup(groupId: Long): Response<Void>

    suspend fun modifyGroup(groupId: Long, name: String): Response<Void>

}