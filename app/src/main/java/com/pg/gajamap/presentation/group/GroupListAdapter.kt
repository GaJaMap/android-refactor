package com.pg.gajamap.presentation.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pg.gajamap.data.model.GroupEntity
import com.pg.gajamap.databinding.ItemGroupListBinding

class GroupListAdapter(private val viewModel: GroupViewModel) :
    ListAdapter<GroupEntity, GroupListAdapter.GroupListViewHolder>(GroupDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupListViewHolder {
        return GroupListViewHolder(
            ItemGroupListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: GroupListViewHolder, position: Int) {
        val itemView = currentList[position]
        holder.bind(viewModel, itemView)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(itemView)
        }
    }

    inner class GroupListViewHolder(private val binding: ItemGroupListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: GroupViewModel, itemView: GroupEntity) {
            binding.viewModel = viewModel
            binding.item = itemView
            binding.executePendingBindings()

            if (itemView.groupId == -1L) {
                binding.ivModify.visibility = View.GONE
                binding.ivDelete.visibility = View.GONE
            }

            binding.ivModify.setOnClickListener {
                onModifyClickListener?.invoke(itemView)
            }

            binding.ivDelete.setOnClickListener {
                onDeleteClickListener?.invoke(itemView)
            }
        }
    }

    private var onItemClickListener: ((GroupEntity) -> Unit)? = null
    fun setOnItemClickListener(listener: (GroupEntity) -> Unit) {
        onItemClickListener = listener
    }

    private var onModifyClickListener: ((GroupEntity) -> Unit)? = null
    fun setOnModifyClickListener(listener: (GroupEntity) -> Unit) {
        onModifyClickListener = listener
    }

    private var onDeleteClickListener: ((GroupEntity) -> Unit)? = null
    fun setOnDeleteClickListener(listener: (GroupEntity) -> Unit) {
        onDeleteClickListener = listener
    }

    companion object {
        private val GroupDiffCallback = object : DiffUtil.ItemCallback<GroupEntity>() {
            override fun areItemsTheSame(oldItem: GroupEntity, newItem: GroupEntity): Boolean {
                return oldItem.groupId == newItem.groupId
            }

            override fun areContentsTheSame(oldItem: GroupEntity, newItem: GroupEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}