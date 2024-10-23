package com.pg.gajamap.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pg.gajamap.data.model.request.MemoRequest
import com.pg.gajamap.data.model.response.memo.Memo
import com.pg.gajamap.data.model.response.memo.MemoResponse
import com.pg.gajamap.data.service.MemoService
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class MemoRepositoryImpl @Inject constructor(
    private val service: MemoService
) : MemoRepository {
    override suspend fun getMemo(
        clientId: Long,
        page: Int
    ): Response<MemoResponse> {
        return service.getMemo(clientId, page)
    }

    override suspend fun createMemo(clientId: Long, memoRequest: MemoRequest): Response<Void> {
        return service.createMemo(clientId, memoRequest)
    }

    override suspend fun deleteMemo(memoId: Int): Response<Void> {
        return service.deleteMemo(memoId)
    }

    override fun getMemosPaging(clientId: Long): Flow<PagingData<Memo>> {
        val pagingSourceFactory = { MemosPagingSource(clientId, service) }

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                maxSize = PAGE_SIZE * 3
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    companion object {
        const val PAGE_SIZE = 1
    }
}
