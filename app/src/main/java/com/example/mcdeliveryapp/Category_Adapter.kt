package com.example.mcdeliveryapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(private val categories: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtName)
        val image: ImageView = view.findViewById(R.id.imgFood)
        val price: TextView = view.findViewById(R.id.txtPrice) // We will hide this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        holder.name.text = category.title
        holder.image.setImageResource(category.imageRes)

        // HIDE THE PRICE for the category selection screen
        holder.price.visibility = View.GONE

        // When clicked, go to the FoodListActivity to show actual products
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, FoodListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", category.title)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = categories.size
}