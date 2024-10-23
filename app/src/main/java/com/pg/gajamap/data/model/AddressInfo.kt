package com.pg.gajamap.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressInfo(
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val groupState: GroupState
) : Parcelable
