package com.pg.gajamap.data.model.response.place


import com.google.gson.annotations.SerializedName

data class PlaceResponse(
    @SerializedName("documents")
    val documents: List<Document>,
    @SerializedName("meta")
    val meta: Meta
)