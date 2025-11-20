package com.bartixxx.opflashcontrol

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bartixxx.opflashcontrol.databinding.ItemSupporterBinding
import com.bumptech.glide.Glide

/**
 * An adapter for the supporters RecyclerView.
 *
 * This adapter is used to display a list of supporters in a RecyclerView.
 *
 * @param supporters The list of supporters to display.
 */
class SupportersAdapter(private val supporters: List<Supporter>) :
    RecyclerView.Adapter<SupportersAdapter.SupporterViewHolder>() {

    /**
     * A ViewHolder for the supporters RecyclerView.
     *
     * @param binding The binding for the supporter item.
     */
    class SupporterViewHolder(val binding: ItemSupporterBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Called when the RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupporterViewHolder {
        val binding =
            ItemSupporterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SupporterViewHolder(binding)
    }

    /**
     * Called by the RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: SupporterViewHolder, position: Int) {
        val supporter = supporters[position]
        holder.binding.supporterName.text = supporter.name
        holder.binding.supporterInfo.text = supporter.info

        // Load the avatar image using Glide
        Glide.with(holder.itemView.context)
            .load(supporter.avatarUrl) // Avatar URL
            .placeholder(R.drawable.ic_launcher_background) // Placeholder image
            .error(R.drawable.ic_launcher_background) // Error image (optional)
            .into(holder.binding.avatarImage)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = supporters.size
}
