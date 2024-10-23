package com.pg.gajamap.data.model.response.memo


import com.google.gson.annotations.SerializedName

data class Memo(
    @SerializedName("memoId")
    val memoId: Int,
    @SerializedName("memoType")
    val memoType: String,
    @SerializedName("message")
    val message: String
)