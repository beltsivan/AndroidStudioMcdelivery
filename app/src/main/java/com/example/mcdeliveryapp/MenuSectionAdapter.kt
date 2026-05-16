package com.example.mcdeliveryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

sealed class SectionedItem {
    data class Header(val categoryName: String) : SectionedItem()
    data class FoodItem(val food: Food) : SectionedItem()
}

class MenuSectionAdapter(
    private val items: List<SectionedItem>,
    private val onFoodClick: (Food) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_FOOD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SectionedItem.Header -> TYPE_HEADER
            is SectionedItem.FoodItem -> TYPE_FOOD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_food, parent, false)
                FoodViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SectionedItem.Header -> {
                (holder as HeaderViewHolder).name.text = item.categoryName
            }
            is SectionedItem.FoodItem -> {
                val vh = holder as FoodViewHolder
                val food = item.food
                vh.name.text = food.name
                vh.price.text = String.format("\u20B1%.2f", food.price)
                loadImage(vh.image, food.image)
                vh.itemView.setOnClickListener { onFoodClick(food) }
            }
        }
    }

    override fun getItemCount() = items.size

    fun attachSpanSizeLookup(layoutManager: GridLayoutManager) {
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (items[position]) {
                    is SectionedItem.Header -> layoutManager.spanCount
                    is SectionedItem.FoodItem -> 1
                }
            }
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.sectionHeaderName)
    }

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtName)
        val image: ImageView = view.findViewById(R.id.imgFood)
        val price: TextView = view.findViewById(R.id.txtPrice)
    }
}
