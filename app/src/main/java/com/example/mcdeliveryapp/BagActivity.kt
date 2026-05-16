package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BagActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_bag)

        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmpty)
        val recyclerBag = findViewById<RecyclerView>(R.id.recyclerBag)

        if (CartManager.cartList.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            recyclerBag.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            recyclerBag.visibility = View.VISIBLE

            recyclerBag.layoutManager = LinearLayoutManager(this)
            recyclerBag.adapter = BagAdapter(CartManager.cartList)
        }
    }
}
