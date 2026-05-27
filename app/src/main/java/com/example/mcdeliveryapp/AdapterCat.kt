package com.example.mcdeliveryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class AdapterCat(
    private val items: List<Category>,
    private val onCategoryClick: ((Category) -> Unit)? = null
) : RecyclerView.Adapter<AdapterCat.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.categoryImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        loadImage(holder.image, item.image)
        holder.itemView.setOnClickListener { onCategoryClick?.invoke(item) }
    }

    override fun getItemCount() = items.size
}