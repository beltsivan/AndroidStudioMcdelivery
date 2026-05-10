package com.example.mcdeliveryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val list: List<MenuCategory>,
    private val onCategoryClick: (MenuCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtName)
        val image: ImageView = view.findViewById(R.id.imgFood)
        val price: TextView = view.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.name.text = item.name
        holder.price.visibility = View.GONE

        val context = holder.itemView.context
        val resId = context.resources.getIdentifier(item.image, "drawable", context.packageName)
        if (resId != 0) {
            holder.image.setImageResource(resId)
            holder.image.contentDescription = item.name
        } else {
            holder.image.setImageDrawable(null)
            holder.image.contentDescription = null
        }

        holder.itemView.setOnClickListener { onCategoryClick(item) }
    }

    override fun getItemCount() = list.size
}
