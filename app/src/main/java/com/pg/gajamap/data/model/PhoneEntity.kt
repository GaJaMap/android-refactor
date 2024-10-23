package com.pg.gajamap.data.model

data class PhoneEntity(
    val contactsId: Int,
    val name: String,
    val number: String,
    var isChecked: Boolean = false
)