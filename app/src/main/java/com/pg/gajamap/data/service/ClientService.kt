package com.pg.gajamap.data.service

import com.pg.gajamap.data.model.request.DeleteRequest
import com.pg.gajamap.data.model.request.upload.UploadRequest
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.data.model.response.client.ClientResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientService {

    @GET("/api/clients")
    suspend fun getAllClient(@Query("wordCond") wordCond: String): Response<ClientResponse>

    @GET("/api/group/{groupId}/clients")
    suspend fun getGroupClient(
        @Path("groupId") groupId: Long,
        @Query("wordCond") wordCond: String
    ): Response<ClientResponse>

    @Multipart
    @POST("/api/clients")
    suspend fun postClient(
        @Part("clientName") clientName: RequestBody,
        @Part("groupId") groupId: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody,
        @Part("mainAddress") mainAddress: RequestBody,
        @Part("detail") detail: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part clientImage: MultipartBody.Part?,
        @Part("isBasicImage") isBasicImage: RequestBody
    ): Response<Client>

    @Multipart
    @PUT("/api/group/{groupId}/clients/{clientId}")
    suspend fun putClient(
        @Path("groupId") groupIdPath: Long,
        @Path("clientId") clientId: Long,
        @Part("clientName") clientName: RequestBody,
        @Part("groupId") groupIdPart: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody,
        @Part("mainAddress") mainAddress: RequestBody,
        @Part("detail") detail: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part clientImage: MultipartBody.Part?,
        @Part("isBasicImage") isBasicImage: RequestBody
    ): Response<Client>

    @POST("api/clients/bulk")
    suspend fun postUploadClient(@Body uploadRequest: UploadRequest): Response<List<Int>>

    @DELETE("/api/group/{groupId}/clients/{clientId}")
    suspend fun deleteClient(
        @Path("groupId") groupId: Long,
        @Path("clientId") clientId: Long
    ): Response<Void>

    @POST("/api/group/{groupId}/clients/bulk-delete")
    suspend fun deleteAnyClient(
        @Path("groupId") groupId: Long,
        @Body deleteRequest: DeleteRequest
    ): Response<Void>

    @GET("/api/clients/nearby")
    suspend fun wholeRadius(
        @Query("radius") radius: Int,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<ClientResponse>

    // 그룹 고객 대상 반경 검색
    @GET("/api/group/{groupId}/clients/nearby")
    suspend fun specificRadius(
        @Path("groupId") groupId: Long,
        @Query("radius") radius: Int,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<ClientResponse>

}