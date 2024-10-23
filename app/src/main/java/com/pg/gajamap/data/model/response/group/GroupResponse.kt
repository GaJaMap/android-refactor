package com.pg.gajamap.data.model.response.group

import com.google.gson.annotations.SerializedName

data class GroupResponse(
    @SerializedName("groupInfos")
    val groupInfos: List<GroupInfo>,
    @SerializedName("hasNext")
    val hasNext: Boolean
)