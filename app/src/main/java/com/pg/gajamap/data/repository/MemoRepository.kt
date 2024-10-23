package com.pg.gajamap.data.repository

import androidx.paging.PagingData
import com.pg.gajamap.data.model.request.MemoRequest
import com.pg.gajamap.data.model.response.memo.Memo
import com.pg.gajamap.data.model.response.memo.MemoResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface MemoRepository {

    suspend fun getMemo(
        clientId: Long,
        page: Int
    ): Response<MemoResponse>

    suspend fun createMemo(clientId: Long, memoRequest: MemoRequest): Response<Void>

    suspend fun deleteMemo(memoId: Int): Response<Void>

    fun getMemosPaging(clientId: Long): Flow<PagingData<Memo>>

}