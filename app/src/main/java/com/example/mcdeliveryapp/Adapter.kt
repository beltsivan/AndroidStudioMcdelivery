package com.example.mcdeliveryapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(private val list: List<Food>) :
    RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.txtName)
        val price = view.findViewById<TextView>(R.id.txtPrice)
        val image = view.findViewById<ImageView>(R.id.imgFood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // 1. Set the data to the views
        holder.name.text = item.name


        holder.price.text = String.format("₱%.2f", item.price)

        holder.image.setImageResource(item.image)

        // 2. Set the click listener to open the Order Details UI
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            // Create intent to your Order Details Activity
            val intent = Intent(context, OrderDetailsActivity::class.java)

            // Pass the data individually to avoid the Parcelable error
            intent.putExtra("FOOD_NAME", item.name)
            intent.putExtra("FOOD_PRICE", item.price) // Passes as Double
            intent.putExtra("FOOD_IMAGE", item.image)

            context.startActivity(intent)
        }
        val density = holder.itemView.context.resources.displayMetrics.density
        val heightInPx = (210 * density).toInt()

        holder.itemView.layoutParams.height = heightInPx
    }


    override fun getItemCount() = list.size
}

