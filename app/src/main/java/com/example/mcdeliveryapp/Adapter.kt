package com.example.mcdeliveryapp

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(
    private val list: List<Food>,
    private val onFoodClick: (Food) -> Unit
) :
    RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.txtName)
        val image = view.findViewById<ImageView>(R.id.imgFood)
        val price = view.findViewById<TextView>(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.name.text = item.name
        holder.price.visibility = View.VISIBLE
        holder.price.text = String.format("₱%.2f", item.price)

        loadImage(holder.image, item.image)
        holder.image.contentDescription = item.name

        holder.itemView.setOnClickListener { onFoodClick(item) }
    }


    override fun getItemCount() = list.size
}

