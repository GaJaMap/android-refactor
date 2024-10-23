package com.pg.gajamap.data.service

import com.pg.gajamap.BuildConfig
import com.pg.gajamap.data.model.response.address.AddressResponse
import com.pg.gajamap.data.model.response.place.PlaceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface KakaoService {

    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
    @GET("/v2/local/search/keyword")
    suspend fun getPlace(
        @Query("query") query: String,
        @Query("page") page: Int
    ): Response<PlaceResponse>

    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
    @GET("/v2/local/geo/coord2address")
    suspend fun getAddress(
        @Query("x") x: String,
        @Query("y") y: String
    ): Response<AddressResponse>
}