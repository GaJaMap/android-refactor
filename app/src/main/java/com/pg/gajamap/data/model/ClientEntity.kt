package com.pg.gajamap.data.model

import com.pg.gajamap.data.model.response.client.Client

data class ClientEntity(
    val client: Client,
    var isChecked: Boolean = false
)