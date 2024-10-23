package com.pg.gajamap.data.model.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("authority")
    val authority: String,
    @SerializedName("createdDate")
    val createdDate: String,
    @SerializedName("email")
    val email: String
)