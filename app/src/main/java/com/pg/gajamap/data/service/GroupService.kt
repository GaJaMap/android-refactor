package com.pg.gajamap.data.service

import com.pg.gajamap.data.model.response.group.GroupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupService {

    @POST("/api/group")
    suspend fun createGroup(@Body name: String): Response<Long>

    @GET("/api/group")
    suspend fun checkGroup(@Query("page") page: Int): Response<GroupResponse>

    @DELETE("/api/group/{groupId}")
    suspend fun deleteGroup(@Path("groupId") groupId: Long): Response<Void>

    @PUT("/api/group/{groupId}")
    suspend fun modifyGroup(@Path("groupId") groupId: Long, @Body name: String): Response<Void>

}