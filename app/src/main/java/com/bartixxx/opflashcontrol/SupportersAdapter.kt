package com.bartixxx.opflashcontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bartixxx.opflashcontrol.databinding.ItemSupporterBinding

class SupportersAdapter(private val supporters: List<Supporter>) :
    RecyclerView.Adapter<SupportersAdapter.SupporterViewHolder>() {

    class SupporterViewHolder(val binding: ItemSupporterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupporterViewHolder {
        val binding = ItemSupporterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SupporterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupporterViewHolder, position: Int) {
        val supporter = supporters[position]
        holder.binding.supporterName.text = supporter.name
        holder.binding.supporterInfo.text = supporter.info
    }

    override fun getItemCount(): Int = supporters.size
}
