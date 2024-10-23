package com.pg.gajamap.presentation.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pg.gajamap.R
import com.pg.gajamap.data.model.response.place.Document
import com.pg.gajamap.databinding.ItemPlaceListBinding

class PlaceListAdapter :
    ListAdapter<Document, PlaceListAdapter.PlaceListViewHolder>(PlaceDiffCallback) {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceListViewHolder {
        return PlaceListViewHolder(
            ItemPlaceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlaceListViewHolder, position: Int) {
        val itemView = currentList[position]
        holder.bind(itemView)

        holder.itemView.setOnClickListener {
            val previouslySelectedPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            notifyItemChanged(previouslySelectedPosition)
            notifyItemChanged(selectedPosition)

            onItemClickListener?.invoke(itemView)
        }

        holder.itemView.setBackgroundResource(if (holder.adapterPosition == selectedPosition) R.color.inform else R.color.white)
    }

    inner class PlaceListViewHolder(private val binding: ItemPlaceListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemView: Document) {
            binding.tvName.text = itemView.placeName

            binding.tvAddress.text = itemView.roadAddressName?.ifBlank { itemView.addressName }

            binding.btnPlus.visibility =
                if (adapterPosition == selectedPosition) View.VISIBLE else View.GONE

            binding.btnPlus.setOnClickListener {
                onAddClickListener?.invoke(itemView)
            }
        }
    }

    private fun clearSelection() {
        val previouslySelectedPosition = selectedPosition
        selectedPosition = -1
        if (previouslySelectedPosition != -1) {
            notifyItemChanged(previouslySelectedPosition)
        }
    }

    fun submitData(newItems: List<Document>) {
        clearSelection()
        submitList(newItems)
    }

    private var onItemClickListener: ((Document) -> Unit)? = null
    fun setOnItemClickListener(listener: (Document) -> Unit) {
        onItemClickListener = listener
    }

    private var onAddClickListener: ((Document) -> Unit)? = null
    fun setOnAddClickListener(listener: (Document) -> Unit) {
        onAddClickListener = listener
    }

    companion object {
        private val PlaceDiffCallback = object : DiffUtil.ItemCallback<Document>() {
            override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean {
                return oldItem == newItem
            }
        }
    }
}