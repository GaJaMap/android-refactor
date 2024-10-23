package com.pg.gajamap.data.model.response.address


import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("address")
    val address: Address,
    @SerializedName("road_address")
    val roadAddress: RoadAddress
)