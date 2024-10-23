package com.pg.gajamap.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class GroupEntity(
    val groupId: Long,
    var groupName: String,
    val clientCount: Int,
) : Parcelable