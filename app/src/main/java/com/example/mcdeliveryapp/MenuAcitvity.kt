package com.example.mcdeliveryapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_menu) // We will create this layout next

        val recycler = findViewById<RecyclerView>(R.id.menuRecyclerView)

        val categories = listOf(
            Category(1, "Zodiac Meals", R.drawable.zodiac),
            Category(2, "Group Meals", R.drawable.groupmeals),
            Category(3, "Chicken & Fish", R.drawable.chickenfish),
            Category(4, "Burgers", R.drawable.burgers),
            Category(5, "Spaghetti", R.drawable.spag),
            Category(6, "Rice Bowls", R.drawable.ricebowl)
        )

        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = CategoryAdapter(categories)

    }
}