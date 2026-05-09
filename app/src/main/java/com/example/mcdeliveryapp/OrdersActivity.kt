package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class OrdersActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        setupBottomNav()
    }

    private fun setupBottomNav() {
        // Home
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        // Menu
        findViewById<LinearLayout>(R.id.navMenu).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        // Orders — already here, do nothing
        findViewById<LinearLayout>(R.id.navOrders).setOnClickListener { /* active tab */ }

        // Coupons
        findViewById<LinearLayout>(R.id.navCoupons).setOnClickListener {
            startActivity(Intent(this, CouponsActivity::class.java))
            finish()
        }

        // More — placeholder
        findViewById<LinearLayout>(R.id.navMore).setOnClickListener { /* TODO */ }
    }
}
