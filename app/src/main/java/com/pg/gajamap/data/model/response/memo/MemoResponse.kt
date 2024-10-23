package com.pg.gajamap.data.model.response.memo


import com.google.gson.annotations.SerializedName

data class MemoResponse(
    @SerializedName("hasNext")
    val hasNext: Boolean,
    @SerializedName("memos")
    val memos: List<Memo>
)