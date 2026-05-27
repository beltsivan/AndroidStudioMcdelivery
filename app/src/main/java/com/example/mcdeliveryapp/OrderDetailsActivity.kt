package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Variable to keep track of the quantity
    private var quantity = 1
    private var basePrice = 0.0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_full_bag)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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

        val foodId = intent.getStringExtra("FOOD_ID") ?: ""
        val foodName = intent.getStringExtra("FOOD_NAME")
        basePrice = intent.getDoubleExtra("FOOD_PRICE", 0.0)
        val foodImage = intent.getStringExtra("FOOD_IMAGE") ?: ""
        val foodCategoryId = intent.getStringExtra("FOOD_CATEGORY_ID") ?: ""
        val foodOrder = intent.getIntExtra("FOOD_ORDER", 0)
        val foodAvailable = intent.getBooleanExtra("FOOD_AVAILABLE", true)

        txtName.text = foodName

        loadImage(imgDetail, foodImage)

        val txtUnavailableMsg = findViewById<TextView>(R.id.txtUnavailableMsg)

        if (!foodAvailable) {
            txtUnavailableMsg.visibility = View.VISIBLE
            btnAddBag.isEnabled = false
            btnAddBag.alpha = 0.5f
            btnAddBag.text = "Not Available"
            btnPlus.visibility = View.GONE
            btnMinus.visibility = View.GONE
            txtQty.visibility = View.GONE
            val cm = ColorMatrix().apply { setSaturation(0f) }
            imgDetail.colorFilter = ColorMatrixColorFilter(cm)
        }

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
                id = foodId,
                name = nameToSave,
                price = basePrice,
                image = foodImage,
                categoryId = foodCategoryId,
                order = foodOrder,
                quantity = quantity
            )
            CartManager.addItem(addedFood)
            val user = auth.currentUser
            if (user != null) {
                CartManager.saveCartToFirestore(db, user.uid)
            }
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