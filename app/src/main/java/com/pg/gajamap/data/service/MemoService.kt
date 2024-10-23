package com.pg.gajamap.data.service

import com.pg.gajamap.data.model.request.MemoRequest
import com.pg.gajamap.data.model.response.memo.MemoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MemoService {

    @GET("/api/memo/client/{clientId}")
    suspend fun getMemo(
        @Path("clientId") clientId: Long,
        @Query("page") page: Int
    ): Response<MemoResponse>

    @POST("/api/memo/client/{clientId}")
    suspend fun createMemo(
        @Path("clientId") clientId: Long,
        @Body memoRequest: MemoRequest
    ): Response<Void>

    @DELETE("/api/memo/{memoId}")
    suspend fun deleteMemo(@Path("memoId") memoId: Int): Response<Void>

}