package com.pg.gajamap.presentation.client

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.databinding.ItemCustomerListBinding
import com.pg.gajamap.util.SortType

class CustomerListAdapter :
    ListAdapter<Client, CustomerListAdapter.CustomerListViewHolder>(CustomerDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerListViewHolder {
        return CustomerListViewHolder(
            ItemCustomerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CustomerListViewHolder, position: Int) {
        val itemView = currentList[position]
        holder.bind(itemView)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(itemView)
        }
    }

    inner class CustomerListViewHolder(private val binding: ItemCustomerListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemView: Client) {
            binding.item = itemView
            binding.executePendingBindings()
            binding.items = arrayOf("카카오 내비", "네이버 내비")

        }
    }

    private var onItemClickListener: ((Client) -> Unit)? = null
    fun setOnItemClickListener(listener: (Client) -> Unit) {
        onItemClickListener = listener
    }

    fun sortData(clientList: List<Client>, sortType: SortType) {
        val sortedList = when (sortType) {
            SortType.NEWEST -> clientList.sortedByDescending { it.createdAt }
            SortType.OLDEST -> clientList.sortedBy { it.createdAt }
            SortType.DISTANCE -> clientList.sortedBy { it.distance }
        }
        submitList(sortedList)
    }

    companion object {
        private val CustomerDiffCallback = object : DiffUtil.ItemCallback<Client>() {
            override fun areItemsTheSame(oldItem: Client, newItem: Client): Boolean {
                return oldItem.clientId == newItem.clientId
            }

            override fun areContentsTheSame(oldItem: Client, newItem: Client): Boolean {
                return oldItem == newItem
            }
        }
    }
}