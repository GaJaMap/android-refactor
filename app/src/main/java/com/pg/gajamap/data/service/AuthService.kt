package com.pg.gajamap.data.service

import com.pg.gajamap.data.model.request.LoginRequest
import com.pg.gajamap.data.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface AuthService {

    @POST("/api/user/login")
    suspend fun postLogin(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @DELETE("/api/user")
    suspend fun withdraw(): Response<Void>
}