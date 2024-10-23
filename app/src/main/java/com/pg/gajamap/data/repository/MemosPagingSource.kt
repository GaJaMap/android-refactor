package com.pg.gajamap.data.repository

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pg.gajamap.data.model.response.memo.Memo
import com.pg.gajamap.data.service.MemoService
import java.io.IOException

class MemosPagingSource(
    private val clientId: Long,
    private val memoService: MemoService
) : PagingSource<Int, Memo>() {

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Memo> {
        return try {
            val pageNumber = params.key ?: STARTING_PAGE_INDEX
            val response = memoService.getMemo(clientId, pageNumber)

            if (response.isSuccessful) {
                val memoResponse = response.body()
                val data = memoResponse?.memos ?: emptyList()
                val hasNextPage = memoResponse?.hasNext ?: false

                val prevKey = if (pageNumber == STARTING_PAGE_INDEX) null else pageNumber - 1
                val nextKey = if (hasNextPage) pageNumber + 1 else null

                LoadResult.Page(
                    data = data,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                LoadResult.Error(IOException("Failed to fetch memos: ${response.message()}"))
            }
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Memo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    companion object {
        const val STARTING_PAGE_INDEX = 0
    }
}