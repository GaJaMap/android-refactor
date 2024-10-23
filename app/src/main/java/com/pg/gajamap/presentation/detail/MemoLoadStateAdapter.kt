package com.pg.gajamap.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pg.gajamap.databinding.ItemLoadStateBinding

class MemoLoadStateAdapter(memoPagingAdapter: MemoPagingAdapter) :
    LoadStateAdapter<MemoLoadStateAdapter.MemoLoadStateViewHolder>() {

    override fun onBindViewHolder(holder: MemoLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): MemoLoadStateViewHolder {
        return MemoLoadStateViewHolder(
            ItemLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    inner class MemoLoadStateViewHolder(
        private val binding: ItemLoadStateBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            binding.progressBar.isVisible = loadState is LoadState.NotLoading
            binding.progressBar.isVisible = loadState is LoadState.Loading
        }
    }
}