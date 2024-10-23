package com.pg.gajamap.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupState(
    val groupId: Long,
    val groupName: String
) : Parcelable