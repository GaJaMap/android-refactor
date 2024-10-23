package com.pg.gajamap.data.repository

import com.pg.gajamap.data.model.response.address.AddressResponse
import com.pg.gajamap.data.model.response.place.PlaceResponse
import com.pg.gajamap.data.service.KakaoService
import retrofit2.Response
import javax.inject.Inject

class KakaoRepositoryImpl @Inject constructor(
    private val service: KakaoService
) : KakaoRepository {

    override suspend fun getAddress(x: String, y: String): Response<AddressResponse> {
        return service.getAddress(x, y)
    }

    override suspend fun getPlace(query: String, page: Int): Response<PlaceResponse> {
        return service.getPlace(query, page)
    }

}