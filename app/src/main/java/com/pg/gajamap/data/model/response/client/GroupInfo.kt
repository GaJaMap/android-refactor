package com.pg.gajamap.data.model.response.client


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupInfo(
    @SerializedName("groupId")
    val groupId: Long,
    @SerializedName("groupName")
    val groupName: String
) : Parcelable