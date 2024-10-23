package com.pg.gajamap.data.model.response.client


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Client(
    @SerializedName("address")
    val address: Address,
    @SerializedName("clientId")
    val clientId: Long,
    @SerializedName("clientName")
    val clientName: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("distance")
    val distance: Double?,
    @SerializedName("groupInfo")
    val groupInfo: GroupInfo,
    @SerializedName("image")
    val image: Image?,
    @SerializedName("location")
    val location: Location,
    @SerializedName("phoneNumber")
    val phoneNumber: String
) : Parcelable