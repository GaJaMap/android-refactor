package com.pg.gajamap.presentation.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.data.model.response.place.Document
import com.pg.gajamap.databinding.ItemPlaceListBinding
import com.pg.gajamap.databinding.ItemSearchListBinding

class SearchListAdapter :
    ListAdapter<Client, SearchListAdapter.SearchListViewHolder>(SearchDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {
        return SearchListViewHolder(
            ItemSearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        val itemView = currentList[position]
        holder.bind(itemView)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(itemView)
        }
    }

    inner class SearchListViewHolder(private val binding: ItemSearchListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemView: Client) {

            binding.tvSearchName.text = itemView.clientName
        }
    }

    private var onItemClickListener: ((Client) -> Unit)? = null
    fun setOnItemClickListener(listener: (Client) -> Unit) {
        onItemClickListener = listener
    }

    companion object {
        private val SearchDiffCallback = object : DiffUtil.ItemCallback<Client>() {
            override fun areItemsTheSame(oldItem: Client, newItem: Client): Boolean {
                return oldItem.clientId == newItem.clientId
            }

            override fun areContentsTheSame(oldItem: Client, newItem: Client): Boolean {
                return oldItem == newItem
            }
        }
    }
}