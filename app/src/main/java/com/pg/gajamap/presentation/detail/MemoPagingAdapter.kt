package com.pg.gajamap.presentation.detail

import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pg.gajamap.R
import com.pg.gajamap.data.model.response.memo.Memo
import com.pg.gajamap.databinding.ItemMemoBinding

class MemoPagingAdapter :
    PagingDataAdapter<Memo, MemoPagingAdapter.MemoViewHolder>(MemoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        return MemoViewHolder(
            ItemMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val pagedItem = getItem(position)
        holder.bind(pagedItem!!)
    }

    inner class MemoViewHolder(private val binding: ItemMemoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemView: Memo) {

            when (itemView.memoType) {
                "CALL" -> {
                    binding.tvContentMemo.text = PhoneNumberUtils.formatNumber(itemView.message)
                    binding.ivMemoIcon.setImageResource(R.drawable.ic_item_phone)
                }

                "NAVIGATION" -> {
                    binding.tvContentMemo.text = itemView.message
                    binding.ivMemoIcon.setImageResource(R.drawable.ic_item_car)
                }

                "MESSAGE" -> {
                    binding.tvContentMemo.text = itemView.message
                    binding.ivMemoIcon.setImageResource(R.drawable.ic_notes_gray)
                }
            }

            binding.ivCloseBtn.setOnClickListener {
                onItemClickListener?.invoke(itemView)
            }
        }
    }

    private var onItemClickListener: ((Memo) -> Unit)? = null
    fun setOnItemClickListener(listener: (Memo) -> Unit) {
        onItemClickListener = listener
    }

    companion object {
        private val MemoDiffCallback = object : DiffUtil.ItemCallback<Memo>() {
            override fun areItemsTheSame(oldItem: Memo, newItem: Memo): Boolean {
                return oldItem.memoId == newItem.memoId
            }

            override fun areContentsTheSame(oldItem: Memo, newItem: Memo): Boolean {
                return oldItem == newItem
            }
        }
    }
}