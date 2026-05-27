package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoryFilterAdapter(
    private val categories: List<Category>,
    private val onCategorySelected: (String?) -> Unit
) : RecyclerView.Adapter<CategoryFilterAdapter.ViewHolder>() {

    private var selectedPosition = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: MaterialCardView = view.findViewById(R.id.chipContainer)
        val name: TextView = view.findViewById(R.id.chipText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val context = holder.itemView.context
        if (position == 0) {
            holder.name.text = "All"
            holder.name.isSelected = selectedPosition == 0
            holder.container.setCardBackgroundColor(
                if (selectedPosition == 0)
                    ContextCompat.getColor(context, R.color.red)
                else
                    ContextCompat.getColor(context, R.color.light_gray)
            )
            holder.name.setTextColor(
                if (selectedPosition == 0)
                    ContextCompat.getColor(context, R.color.white)
                else
                    ContextCompat.getColor(context, R.color.black)
            )
        } else {
            val cat = categories[position - 1]
            holder.name.text = cat.name
            val isSelected = selectedPosition == position
            holder.name.isSelected = isSelected
            holder.container.setCardBackgroundColor(
                if (isSelected)
                    ContextCompat.getColor(context, R.color.red)
                else
                    ContextCompat.getColor(context, R.color.light_gray)
            )
            holder.name.setTextColor(
                if (isSelected)
                    ContextCompat.getColor(context, R.color.white)
                else
                    ContextCompat.getColor(context, R.color.black)
            )
        }

        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = position
            notifyItemChanged(prev)
            notifyItemChanged(position)
            val catId = if (position == 0) null else categories[position - 1].id
            onCategorySelected(catId)
        }
    }

    override fun getItemCount() = categories.size + 1

    fun selectAll() {
        val prev = selectedPosition
        selectedPosition = 0
        notifyItemChanged(prev)
        notifyItemChanged(0)
    }
}
