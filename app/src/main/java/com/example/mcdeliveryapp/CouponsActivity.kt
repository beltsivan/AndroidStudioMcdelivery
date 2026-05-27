package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CouponsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupons)

        db = FirebaseFirestore.getInstance()

        val emptyLayout = findViewById<LinearLayout>(R.id.emptyCouponLayout)
        val recycler = findViewById<RecyclerView>(R.id.recyclerCoupons)
        recycler.layoutManager = LinearLayoutManager(this)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            emptyLayout.visibility = View.VISIBLE
            recycler.visibility = View.GONE
            setupBottomNav()
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val branchId = doc.getString("branchId") ?: ""
                if (branchId.isEmpty()) {
                    emptyLayout.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    setupBottomNav()
                    return@addOnSuccessListener
                }

                db.collection("coupons")
                    .whereEqualTo("branchId", branchId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnSuccessListener { result ->
                        val now = Timestamp.now()
                        val validCoupons = result.documents.filter { couponDoc ->
                            val expiresAt = couponDoc.getTimestamp("expiresAt")
                            val maxUsage = couponDoc.getLong("maxUsage") ?: 0
                            val usedCount = couponDoc.getLong("usedCount") ?: 0
                            (expiresAt == null || expiresAt > now) &&
                            (maxUsage == 0L || usedCount < maxUsage)
                        }

                        if (validCoupons.isEmpty()) {
                            emptyLayout.visibility = View.VISIBLE
                            recycler.visibility = View.GONE
                        } else {
                            emptyLayout.visibility = View.GONE
                            recycler.visibility = View.VISIBLE
                            val data = validCoupons.map { it.data!! }
                            recycler.adapter = CouponAdapter(data)
                        }
                        setupBottomNav()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        emptyLayout.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                        setupBottomNav()
                    }
            }
            .addOnFailureListener {
                emptyLayout.visibility = View.VISIBLE
                recycler.visibility = View.GONE
                setupBottomNav()
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
        findViewById<LinearLayout>(R.id.navOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navCoupons).setOnClickListener { }
        findViewById<LinearLayout>(R.id.navMore).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}
