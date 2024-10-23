package com.pg.gajamap.data.model.response.client


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    @SerializedName("filePath")
    val filePath: String?,
    @SerializedName("originalFileName")
    val originalFileName: String?
) : Parcelable