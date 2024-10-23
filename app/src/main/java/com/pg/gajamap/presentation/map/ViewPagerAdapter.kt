package com.pg.gajamap.presentation.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.databinding.ItemViewpagerBinding

class ViewPagerAdapter(private val client: Client) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    inner class ViewPagerViewHolder(private val binding: ItemViewpagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemView: Client) {
            binding.item = itemView
            binding.executePendingBindings()
            binding.items = arrayOf("카카오 내비", "네이버 내비")

            binding.itemViewpager.setOnClickListener {
                val action =
                    MapFragmentDirections.actionNavigationMapToDetailCustomerFragment(itemView)
                it.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemViewpagerBinding.inflate(inflater, parent, false)
        return ViewPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.bind(client)
    }

    override fun getItemCount(): Int {
        return 1
    }
}