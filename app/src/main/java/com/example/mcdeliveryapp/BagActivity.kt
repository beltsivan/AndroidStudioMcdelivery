package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BagActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_bag)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmpty)
        val recyclerBag = findViewById<RecyclerView>(R.id.recyclerBag)
        val bottomPanel = findViewById<LinearLayout>(R.id.bottomPanel)
        val txtSubtotal = findViewById<TextView>(R.id.txtSubtotal)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        btnCheckout.setOnClickListener {
            Toast.makeText(this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show()
        }

        updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal)
    }

    private fun updateBagUi(
        layoutEmpty: LinearLayout,
        recyclerBag: RecyclerView,
        bottomPanel: LinearLayout,
        txtSubtotal: TextView
    ) {
        if (CartManager.cartList.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            recyclerBag.visibility = View.GONE
            bottomPanel.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            recyclerBag.visibility = View.VISIBLE
            bottomPanel.visibility = View.VISIBLE

            recyclerBag.layoutManager = LinearLayoutManager(this)
            recyclerBag.adapter = BagAdapter(
                list = CartManager.cartList,
                onIncrement = { position ->
                    CartManager.incrementQuantity(position)
                    recyclerBag.adapter?.notifyItemChanged(position)
                    txtSubtotal.text = String.format("\u20B1%.2f", CartManager.getSubtotal())
                },
                onDecrement = { position ->
                    val wasRemoved = CartManager.decrementQuantity(position)
                    if (CartManager.cartList.isEmpty()) {
                        updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal)
                    } else {
                        if (wasRemoved) {
                            recyclerBag.adapter?.notifyItemRemoved(position)
                        } else {
                            recyclerBag.adapter?.notifyItemChanged(position)
                        }
                        txtSubtotal.text = String.format("\u20B1%.2f", CartManager.getSubtotal())
                    }
                }
            )
            txtSubtotal.text = String.format("\u20B1%.2f", CartManager.getSubtotal())
        }
    }

    override fun onResume() {
        super.onResume()
        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmpty)
        val recyclerBag = findViewById<RecyclerView>(R.id.recyclerBag)
        val bottomPanel = findViewById<LinearLayout>(R.id.bottomPanel)
        val txtSubtotal = findViewById<TextView>(R.id.txtSubtotal)
        updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal)
    }
}
