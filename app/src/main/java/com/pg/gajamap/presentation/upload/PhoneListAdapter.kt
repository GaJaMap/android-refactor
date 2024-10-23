package com.pg.gajamap.presentation.upload

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pg.gajamap.data.model.PhoneEntity
import com.pg.gajamap.databinding.ItemPhoneListBinding

class PhoneListAdapter :
    ListAdapter<PhoneEntity, PhoneListAdapter.PhoneListViewHolder>(PhoneDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneListViewHolder {
        return PhoneListViewHolder(
            ItemPhoneListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PhoneListViewHolder, position: Int) {
        val itemView = currentList[position]
        holder.bind(itemView)
    }

    inner class PhoneListViewHolder(private val binding: ItemPhoneListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemView: PhoneEntity) {
            binding.itemPhoneTv.text = itemView.name
            binding.itemPhoneTv.isChecked = itemView.isChecked

            binding.itemPhoneTv.setOnClickListener {
                itemView.isChecked = binding.itemPhoneTv.isChecked
                onItemClickListener?.invoke()
            }
        }
    }

    private var onItemClickListener: (() -> Unit)? = null
    fun setOnItemClickListener(listener: () -> Unit) {
        onItemClickListener = listener
    }

    fun checkAll(phoneList: ArrayList<PhoneEntity>, isChecked: Boolean) {
        phoneList.forEach { it.isChecked = isChecked }
        notifyDataSetChanged()
    }

    companion object {
        private val PhoneDiffCallback = object : DiffUtil.ItemCallback<PhoneEntity>() {
            override fun areItemsTheSame(oldItem: PhoneEntity, newItem: PhoneEntity): Boolean {
                return oldItem.contactsId == newItem.contactsId
            }

            override fun areContentsTheSame(oldItem: PhoneEntity, newItem: PhoneEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}