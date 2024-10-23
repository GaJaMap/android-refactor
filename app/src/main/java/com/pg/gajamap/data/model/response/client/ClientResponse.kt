package com.pg.gajamap.data.model.response.client


import com.google.gson.annotations.SerializedName

data class ClientResponse(
    @SerializedName("clients")
    val clients: List<Client>
)