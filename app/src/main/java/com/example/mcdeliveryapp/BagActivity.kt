package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BagActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_bag)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmpty)
        val recyclerBag = findViewById<RecyclerView>(R.id.recyclerBag)
        val bottomPanel = findViewById<LinearLayout>(R.id.bottomPanel)
        val txtSubtotal = findViewById<TextView>(R.id.txtSubtotal)
        val txtDeliveryFee = findViewById<TextView>(R.id.txtDeliveryFee)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupPayment)
        val btnPlaceOrder = findViewById<Button>(R.id.btnPlaceOrder)

        txtDeliveryFee.text = String.format("\u20B1%.2f", CartManager.DELIVERY_FEE)

        btnPlaceOrder.setOnClickListener {
            placeOrder(radioGroup.checkedRadioButtonId)
        }

        val user = auth.currentUser
        if (user != null) {
            CartManager.loadCartFromFirestore(db, user.uid) {
                updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal, txtTotal)
            }
        } else {
            updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal, txtTotal)
        }
    }

    private fun updateBagUi(
        layoutEmpty: LinearLayout,
        recyclerBag: RecyclerView,
        bottomPanel: LinearLayout,
        txtSubtotal: TextView,
        txtTotal: TextView
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
                    txtTotal.text = String.format("\u20B1%.2f", CartManager.getTotal())
                    val user = auth.currentUser
                    if (user != null) CartManager.saveCartToFirestore(db, user.uid)
                },
                onDecrement = { position ->
                    val wasRemoved = CartManager.decrementQuantity(position)
                    if (CartManager.cartList.isEmpty()) {
                        val user = auth.currentUser
                        if (user != null) CartManager.saveCartToFirestore(db, user.uid)
                        updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal, txtTotal)
                    } else {
                        if (wasRemoved) {
                            recyclerBag.adapter?.notifyItemRemoved(position)
                        } else {
                            recyclerBag.adapter?.notifyItemChanged(position)
                        }
                        txtSubtotal.text = String.format("\u20B1%.2f", CartManager.getSubtotal())
                        txtTotal.text = String.format("\u20B1%.2f", CartManager.getTotal())
                        val user = auth.currentUser
                        if (user != null) CartManager.saveCartToFirestore(db, user.uid)
                    }
                }
            )
            txtSubtotal.text = String.format("\u20B1%.2f", CartManager.getSubtotal())
            txtTotal.text = String.format("\u20B1%.2f", CartManager.getTotal())
        }
    }

    private fun placeOrder(checkedRadioId: Int) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
            return
        }

        if (CartManager.cartList.isEmpty()) {
            Toast.makeText(this, "Your bag is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentMethod =
            if (checkedRadioId == R.id.radioOnline) "Online Payment" else "Cash on Delivery"

        val btnPlaceOrder = findViewById<Button>(R.id.btnPlaceOrder)
        btnPlaceOrder.isEnabled = false
        btnPlaceOrder.text = "Placing Order..."

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val addressRaw = doc.get("address")
                val contactNumber = doc.getString("contactNumber") ?: ""
                val customerName = doc.getString("name") ?: user.displayName ?: "Customer"
                val branchId = doc.getString("branchId") ?: ""
                val branchName = doc.getString("branchName") ?: ""

                if (contactNumber.isEmpty()) {
                    btnPlaceOrder.isEnabled = true
                    btnPlaceOrder.text = "Place Order"
                    showProfileRequiredDialog("Contact Number Required",
                        "Please set your contact number in your profile before placing an order.")
                    return@addOnSuccessListener
                }

                if (addressRaw !is Map<*, *> || addressRaw.isEmpty()) {
                    btnPlaceOrder.isEnabled = true
                    btnPlaceOrder.text = "Place Order"
                    showProfileRequiredDialog("Delivery Address Required",
                        "Please set your delivery address in your profile before placing an order.")
                    return@addOnSuccessListener
                }

                val deliveryAddress = hashMapOf<String, Any>(
                    "street" to ((addressRaw["street"] as? String) ?: ""),
                    "barangay" to ((addressRaw["barangay"] as? String) ?: ""),
                    "municipality" to ((addressRaw["municipality"] as? String) ?: ""),
                    "province" to ((addressRaw["province"] as? String) ?: ""),
                    "postalCode" to ((addressRaw["postalCode"] as? String) ?: "")
                )

                createOrder(user.uid, customerName, contactNumber, deliveryAddress,
                    branchId, branchName, paymentMethod, btnPlaceOrder)
            }
            .addOnFailureListener { e ->
                btnPlaceOrder.isEnabled = true
                btnPlaceOrder.text = "Place Order"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showProfileRequiredDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Go to Profile") { _, _ ->
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createOrder(
        userId: String,
        customerName: String,
        contactNumber: String,
        deliveryAddress: Map<String, Any>,
        branchId: String,
        branchName: String,
        paymentMethod: String,
        btnPlaceOrder: Button
    ) {
        val items = CartManager.cartList.map { food ->
            hashMapOf<String, Any>(
                "id" to food.id,
                "name" to food.name,
                "price" to food.price,
                "quantity" to food.quantity,
                "image" to food.image
            )
        }

        val subtotal = CartManager.getSubtotal()
        val deliveryFee = CartManager.DELIVERY_FEE
        val total = CartManager.getTotal()

        val orderData = hashMapOf<String, Any>(
            "userId" to userId,
            "customerName" to customerName,
            "contactNumber" to contactNumber,
            "deliveryAddress" to deliveryAddress,
            "branchId" to branchId,
            "branchName" to branchName,
            "items" to items,
            "subtotal" to subtotal,
            "deliveryFee" to deliveryFee,
            "total" to total,
            "paymentMethod" to paymentMethod,
            "status" to "Pending",
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("orders")
            .add(orderData)
            .addOnSuccessListener { docRef ->
                CartManager.clearCart()
                CartManager.saveCartToFirestore(db, userId)
                btnPlaceOrder.isEnabled = true
                btnPlaceOrder.text = "Place Order"

                AlertDialog.Builder(this)
                    .setTitle("Order Placed!")
                    .setMessage("Your order has been placed successfully.\nOrder ID: ${docRef.id}")
                    .setPositiveButton("View Orders") { _, _ ->
                        val intent = Intent(this, OrdersActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
            .addOnFailureListener { e ->
                btnPlaceOrder.isEnabled = true
                btnPlaceOrder.text = "Place Order"
                Toast.makeText(this, "Failed to place order: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmpty)
        val recyclerBag = findViewById<RecyclerView>(R.id.recyclerBag)
        val bottomPanel = findViewById<LinearLayout>(R.id.bottomPanel)
        val txtSubtotal = findViewById<TextView>(R.id.txtSubtotal)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)
        updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal, txtTotal)
    }
}
