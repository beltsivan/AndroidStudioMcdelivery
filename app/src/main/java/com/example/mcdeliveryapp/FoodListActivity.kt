package com.example.mcdeliveryapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager // Changed to Grid
import androidx.recyclerview.widget.RecyclerView

class FoodListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_list)

        val categoryName = intent.getStringExtra("CATEGORY_NAME")
        val txtHeader = findViewById<TextView>(R.id.txtCategoryHeader)
        val recycler = findViewById<RecyclerView>(R.id.recyclerFoodList)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Set the header title based on what was clicked
        txtHeader.text = categoryName

        // Back button functionality
        btnBack.setOnClickListener { finish() }

        // Define your Group Meals list (matching your screenshot)
        val groupMealsList = listOf(
            Food(1, "BFF Shake Shake Fries BBQ N' McFloat Combo", 294.0, R.drawable.shakefries),
            Food(2, "6-pc. Chicken McShare Box", 515.0, R.drawable.chickens),
            Food(3, "8-pc. Chicken McShare Box", 675.0, R.drawable.chickenss),
            Food(4, "20-pc. Chicken McNuggets", 375.0, R.drawable.nuggets)
        )

        val burgerList = listOf(
            Food(5, "Big Mac", 150.0, R.drawable.bigmac),
            Food(6, "Cheeseburger", 120.0, R.drawable.cheeseburger)
        )

        // Decide which list to show
        val listToShow = when (categoryName) {
            "Group Meals" -> groupMealsList
            "Burgers" -> burgerList
            else -> groupMealsList
        }

        // 2-column Grid setup like the screenshot
        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = FoodAdapter(listToShow)
    }
}