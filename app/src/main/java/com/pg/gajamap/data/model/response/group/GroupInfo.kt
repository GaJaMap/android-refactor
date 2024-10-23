package com.pg.gajamap.data.model.response.group


import com.google.gson.annotations.SerializedName

data class GroupInfo(
    @SerializedName("clientCount")
    val clientCount: Int,
    @SerializedName("groupId")
    val groupId: Long,
    @SerializedName("groupName")
    val groupName: String
)