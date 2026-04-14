package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class OrderDetailsActivity : AppCompatActivity() {

    // Variable to keep track of the quantity
    private var quantity = 1
    private var basePrice = 0.0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_full_bag)

        val btnBack = findViewById<ImageView>(R.id.btnBack1)


        btnBack.setOnClickListener {

            finish()
        }

        val imgDetail = findViewById<ImageView>(R.id.imgProduct)
        val txtName = findViewById<TextView>(R.id.txtProductName)
        val txtPrice = findViewById<TextView>(R.id.txtProductPrice)
        val txtQty = findViewById<TextView>(R.id.txtQuantity)
        val btnPlus = findViewById<ImageView>(R.id.btnIncrease) // Your + icon
        val btnMinus = findViewById<ImageView>(R.id.btnDecrease) // Your - icon
        val btnAddBag = findViewById<Button>(R.id.btnOrderNow)

        val foodName = intent.getStringExtra("FOOD_NAME")
        basePrice = intent.getDoubleExtra("FOOD_PRICE", 0.0)
        val foodImage = intent.getIntExtra("FOOD_IMAGE", 0)


        txtName.text = foodName
        imgDetail.setImageResource(foodImage)
        updatePriceDisplay(txtPrice)


        btnPlus.setOnClickListener {
            quantity++
            txtQty.text = quantity.toString()
            updatePriceDisplay(txtPrice)
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                txtQty.text = quantity.toString()
                updatePriceDisplay(txtPrice)
            }
        }

        btnAddBag.setOnClickListener {

            val nameToSave = foodName ?: "Unknown Food"
            val addedFood = Food(
                id = (0..1000).random(), // Simple random ID
                name = "$quantity x $nameToSave",
                price = basePrice * quantity,
                image = foodImage
            )
            CartManager.cartList.add(addedFood)
            Toast.makeText(this, "Added to bag!", Toast.LENGTH_SHORT).show()
            finish()
        }

    }
    // Helper function to update the price text based on quantity
    private fun updatePriceDisplay(priceTextView: TextView) {
        val totalPrice = basePrice * quantity
        priceTextView.text = String.format("₱%.2f", totalPrice)
    }
}