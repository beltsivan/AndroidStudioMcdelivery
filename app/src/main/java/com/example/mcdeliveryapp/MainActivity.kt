package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val recycler = findViewById<RecyclerView>(R.id.recyclerFood)


        val foodList = listOf(
            Food(1,"Crispy Chicken Fillet Meal", 177.00, R.drawable.burgfillet),
            Food(2,"Burger McDo Meal", 157.00, R.drawable.burgermenu)
        )

        recycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recycler.adapter = FoodAdapter(foodList)

        val btnOpenBag = findViewById<ImageView>(R.id.bag)

        btnOpenBag.setOnClickListener {
            // Create an Intent to go from this Activity to BagActivity
            val intent = Intent(this, BagActivity::class.java)
            startActivity(intent)
        }


    }
}