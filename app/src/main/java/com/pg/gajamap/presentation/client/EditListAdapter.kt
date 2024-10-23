package com.pg.gajamap.presentation.client

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pg.gajamap.R
import com.pg.gajamap.data.model.ClientEntity
import com.pg.gajamap.databinding.ItemEditListBinding

class EditListAdapter :
    ListAdapter<ClientEntity, EditListAdapter.EditListViewHolder>(CustomerDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditListViewHolder {
        return EditListViewHolder(
            ItemEditListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: EditListViewHolder, position: Int) {
        val itemView = currentList[position]
        holder.bind(itemView)
    }

    inner class EditListViewHolder(private val binding: ItemEditListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemView: ClientEntity) {
            binding.item = itemView
            binding.executePendingBindings()

            binding.layoutContainer.setBackgroundResource(if (itemView.isChecked) R.drawable.fragment_list_tool_purple else R.drawable.fragment_list_tool)
            binding.layoutContainer.setOnClickListener {
                itemView.isChecked = itemView.isChecked != true
                onItemClickListener?.invoke(itemView)
                notifyDataSetChanged()
            }
        }
    }

    fun checkAll(clientList: ArrayList<ClientEntity>, isChecked: Boolean) {
        clientList.forEach { it.isChecked = isChecked }
        notifyDataSetChanged()
    }

    private var onItemClickListener: ((ClientEntity) -> Unit)? = null
    fun setOnItemClickListener(listener: (ClientEntity) -> Unit) {
        onItemClickListener = listener
    }

    companion object {
        private val CustomerDiffCallback = object : DiffUtil.ItemCallback<ClientEntity>() {
            override fun areItemsTheSame(oldItem: ClientEntity, newItem: ClientEntity): Boolean {
                return oldItem.client.clientId == newItem.client.clientId
            }

            override fun areContentsTheSame(oldItem: ClientEntity, newItem: ClientEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}