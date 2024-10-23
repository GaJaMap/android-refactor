package com.pg.gajamap.data.repository

import com.pg.gajamap.data.model.response.address.AddressResponse
import com.pg.gajamap.data.model.response.place.PlaceResponse
import retrofit2.Response

interface KakaoRepository {

    suspend fun getAddress(x: String, y: String): Response<AddressResponse>

    suspend fun getPlace(query: String, page: Int): Response<PlaceResponse>

}