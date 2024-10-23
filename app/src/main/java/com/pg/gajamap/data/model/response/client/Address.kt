package com.pg.gajamap.data.model.response.client


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    @SerializedName("detail")
    val detail: String?,
    @SerializedName("mainAddress")
    val mainAddress: String?
) : Parcelable