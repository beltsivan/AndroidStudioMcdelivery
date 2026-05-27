package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrdersActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val ordersList = mutableListOf<Map<String, Any>>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupBottomNav()
        loadOrders()
    }

    private fun loadOrders() {
        val user = auth.currentUser
        if (user == null) return

        val emptyLayout = findViewById<LinearLayout>(R.id.emptyLayout)
        val recyclerOrders = findViewById<RecyclerView>(R.id.recyclerOrders)
        val txtEmptyTitle = findViewById<TextView>(R.id.txtEmptyTitle)
        val txtEmptySubtitle = findViewById<TextView>(R.id.txtEmptySubtitle)
        txtEmptyTitle.text = "Loading..."
        txtEmptySubtitle.visibility = View.GONE

        db.collection("orders")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { result ->
                ordersList.clear()
                for (doc in result.documents) {
                    val data = doc.data
                    if (data != null) {
                        data["orderId"] = doc.id
                        ordersList.add(data)
                    }
                }

                ordersList.sortByDescending {
                    (it["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: java.util.Date(0)
                }

                if (ordersList.isEmpty()) {
                    emptyLayout.visibility = View.VISIBLE
                    recyclerOrders.visibility = View.GONE
                    txtEmptyTitle.text = "No orders yet"
                    txtEmptySubtitle.text = "Your placed orders will appear here."
                    txtEmptySubtitle.visibility = View.VISIBLE
                } else {
                    emptyLayout.visibility = View.GONE
                    recyclerOrders.visibility = View.VISIBLE
                    recyclerOrders.layoutManager = LinearLayoutManager(this)
                    recyclerOrders.adapter = OrderAdapter(ordersList)
                }
            }
            .addOnFailureListener { e ->
                emptyLayout.visibility = View.VISIBLE
                recyclerOrders.visibility = View.GONE
                txtEmptyTitle.text = "Failed to load orders"
                txtEmptySubtitle.text = e.message ?: "Please check your connection and try again."
                txtEmptySubtitle.visibility = View.VISIBLE
            }
    }

    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navMenu).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navOrders).setOnClickListener { /* active tab */ }

        findViewById<LinearLayout>(R.id.navCoupons).setOnClickListener {
            startActivity(Intent(this, CouponsActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navMore).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}
