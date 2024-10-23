package com.pg.gajamap.data.model.request.upload


import com.google.gson.annotations.SerializedName

data class Client(
    @SerializedName("clientName")
    val clientName: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String
)