package com.pg.gajamap.data.model.request.upload


import com.google.gson.annotations.SerializedName

data class UploadRequest(
    @SerializedName("clients")
    val clients: List<Client>,
    @SerializedName("groupId")
    val groupId: Long
)