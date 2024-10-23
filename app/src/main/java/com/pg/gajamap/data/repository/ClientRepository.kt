package com.pg.gajamap.data.repository

import com.pg.gajamap.data.model.request.DeleteRequest
import com.pg.gajamap.data.model.request.upload.UploadRequest
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.data.model.response.client.ClientResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

interface ClientRepository {

    suspend fun getAllClient(wordCond: String): Response<ClientResponse>

    suspend fun getGroupClient(groupId: Long, wordCond: String): Response<ClientResponse>

    suspend fun postClient(
        clientName: RequestBody,
        groupId: RequestBody,
        phoneNumber: RequestBody,
        mainAddress: RequestBody,
        detail: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        clientImage: MultipartBody.Part?,
        isBasicImage: RequestBody
    ): Response<Client>

    suspend fun postUploadClient(uploadRequest: UploadRequest): Response<List<Int>>

    suspend fun putClient(
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
    ): Response<Client>

    suspend fun deleteClient(groupId: Long, clientId: Long): Response<Void>

    suspend fun deleteAnyClient(groupId: Long, deleteRequest: DeleteRequest): Response<Void>

    suspend fun wholeRadius(
        radius: Int,
        latitude: Double,
        longitude: Double
    ): Response<ClientResponse>

    suspend fun specificRadius(
        groupId: Long,
        radius: Int,
        latitude: Double,
        longitude: Double
    ): Response<ClientResponse>
}