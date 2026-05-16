package com.example.mcdeliveryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BagAdapter(
    private val list: List<Food>
) : RecyclerView.Adapter<BagAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.bagItemImage)
        val name: TextView = view.findViewById(R.id.bagItemName)
        val price: TextView = view.findViewById(R.id.bagItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        holder.price.text = String.format("\u20B1%.2f", item.price)
        loadImage(holder.image, item.image)
    }

    override fun getItemCount() = list.size
}
