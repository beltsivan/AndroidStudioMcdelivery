package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_menu)

        setupBottomNav()
    }

    private fun setupBottomNav() {
        // Home — go to MainActivity and clear back-stack so Back doesn't loop
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        // Menu — already here, do nothing
        findViewById<LinearLayout>(R.id.navMenu).setOnClickListener { /* active tab */ }

        // Orders
        findViewById<LinearLayout>(R.id.navOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
            finish()
        }

        // Coupons
        findViewById<LinearLayout>(R.id.navCoupons).setOnClickListener {
            startActivity(Intent(this, CouponsActivity::class.java))
            finish()
        }

        // More — placeholder (no activity yet)
        findViewById<LinearLayout>(R.id.navMore).setOnClickListener { /* TODO */ }
    }
}
