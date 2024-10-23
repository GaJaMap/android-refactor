package com.pg.gajamap.data.model.response.address


import com.google.gson.annotations.SerializedName

data class Meta(
    @SerializedName("total_count")
    val totalCount: Int
)