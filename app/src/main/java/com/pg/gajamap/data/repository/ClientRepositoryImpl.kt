package com.pg.gajamap.data.repository

import com.pg.gajamap.data.model.request.DeleteRequest
import com.pg.gajamap.data.model.request.upload.UploadRequest
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.data.model.response.client.ClientResponse
import com.pg.gajamap.data.service.ClientService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    private val service: ClientService
) : ClientRepository {

    override suspend fun getAllClient(wordCond: String): Response<ClientResponse> {
        return service.getAllClient(wordCond)
    }

    override suspend fun getGroupClient(groupId: Long, wordCond: String): Response<ClientResponse> {
        return service.getGroupClient(groupId, wordCond)
    }

    override suspend fun postClient(
        clientName: RequestBody,
        groupId: RequestBody,
        phoneNumber: RequestBody,
        mainAddress: RequestBody,
        detail: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        clientImage: MultipartBody.Part?,
        isBasicImage: RequestBody
    ): Response<Client> {
        return service.postClient(
            clientName,
            groupId,
            phoneNumber,
            mainAddress,
            detail,
            latitude,
            longitude,
            clientImage,
            isBasicImage
        )
    }

    override suspend fun postUploadClient(uploadRequest: UploadRequest): Response<List<Int>> {
        return service.postUploadClient(uploadRequest)
    }

    override suspend fun putClient(
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
    ): Response<Client> {
        return service.putClient(
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
    }

    override suspend fun deleteClient(groupId: Long, clientId: Long): Response<Void> {
        return service.deleteClient(groupId, clientId)
    }

    override suspend fun deleteAnyClient(
        groupId: Long,
        deleteRequest: DeleteRequest
    ): Response<Void> {
        return service.deleteAnyClient(groupId, deleteRequest)
    }

    override suspend fun wholeRadius(
        radius: Int,
        latitude: Double,
        longitude: Double
    ): Response<ClientResponse> {
        return service.wholeRadius(radius, latitude, longitude)
    }

    override suspend fun specificRadius(
        groupId: Long,
        radius: Int,
        latitude: Double,
        longitude: Double
    ): Response<ClientResponse> {
        return service.specificRadius(groupId, radius, latitude, longitude)
    }

}