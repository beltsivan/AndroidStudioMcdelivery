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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class BagActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedCouponId: String? = null
    private var selectedCouponCode: String? = null
    private var discountAmount = 0.0

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

        val headerPayment = findViewById<LinearLayout>(R.id.headerPayment)
        val paymentSection = findViewById<LinearLayout>(R.id.paymentSection)
        val txtDropdownArrow = findViewById<TextView>(R.id.txtDropdownArrow)

        headerPayment.setOnClickListener {
            val isVisible = paymentSection.visibility == View.VISIBLE
            paymentSection.visibility = if (isVisible) View.GONE else View.VISIBLE
            txtDropdownArrow.text = if (isVisible) "▾" else "▴"
        }

        val btnApplyCoupon = findViewById<Button>(R.id.btnApplyCoupon)
        val layoutDiscount = findViewById<LinearLayout>(R.id.layoutDiscount)
        val txtDiscount = findViewById<TextView>(R.id.txtDiscount)
        val txtCouponLabel = findViewById<TextView>(R.id.txtCouponLabel)

        txtDeliveryFee.text = String.format("\u20B1%.2f", CartManager.DELIVERY_FEE)

        btnApplyCoupon.setOnClickListener { showCouponPicker() }

        btnPlaceOrder.setOnClickListener {
            placeOrder(radioGroup.checkedRadioButtonId, btnPlaceOrder, txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
        }

        val user = auth.currentUser
        if (user != null) {
            CartManager.loadCartFromFirestore(db, user.uid) {
                updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
            }
        } else {
            updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
        }
    }

    private fun updateBagUi(
        layoutEmpty: LinearLayout,
        recyclerBag: RecyclerView,
        bottomPanel: LinearLayout,
        txtSubtotal: TextView,
        txtTotal: TextView,
        layoutDiscount: LinearLayout,
        txtDiscount: TextView,
        txtCouponLabel: TextView
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
                    refreshTotals(txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
                    val user = auth.currentUser
                    if (user != null) CartManager.saveCartToFirestore(db, user.uid)
                },
                onDecrement = { position ->
                    val wasRemoved = CartManager.decrementQuantity(position)
                    if (CartManager.cartList.isEmpty()) {
                        val user = auth.currentUser
                        if (user != null) CartManager.saveCartToFirestore(db, user.uid)
                        updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
                    } else {
                        if (wasRemoved) {
                            recyclerBag.adapter?.notifyItemRemoved(position)
                        } else {
                            recyclerBag.adapter?.notifyItemChanged(position)
                        }
                        refreshTotals(txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
                        val user = auth.currentUser
                        if (user != null) CartManager.saveCartToFirestore(db, user.uid)
                    }
                }
            )
            refreshTotals(txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
        }
    }

    private fun refreshTotals(
        txtSubtotal: TextView,
        txtTotal: TextView,
        layoutDiscount: LinearLayout,
        txtDiscount: TextView,
        txtCouponLabel: TextView
    ) {
        val subtotal = CartManager.getSubtotal()
        txtSubtotal.text = String.format("\u20B1%.2f", subtotal)

        if (selectedCouponId != null) {
            txtDiscount.text = String.format("- \u20B1%.2f", discountAmount)
            layoutDiscount.visibility = View.VISIBLE
        }

        val total = CartManager.getTotal() - discountAmount
        txtTotal.text = String.format("\u20B1%.2f", total)
    }

    private fun showCouponPicker() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val branchId = doc.getString("branchId") ?: return@addOnSuccessListener
                val subtotal = CartManager.getSubtotal()

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
                            Toast.makeText(this, "No available coupons", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        val items = validCoupons.map { couponDoc ->
                            val code = couponDoc.getString("code") ?: "?"
                            val desc = couponDoc.getString("description") ?: ""
                            val discountType = couponDoc.getString("discountType") ?: "fixed"
                            val discountValue = (couponDoc.getDouble("discountValue") ?: couponDoc.getLong("discountValue")?.toDouble()) ?: 0.0
                            val minOrder = (couponDoc.getDouble("minOrderAmount") ?: couponDoc.getLong("minOrderAmount")?.toDouble()) ?: 0.0
                            val discountStr = if (discountType == "percentage") "${discountValue.toInt()}%" else "\u20B1${String.format("%.0f", discountValue)}"
                            val label = "$code — $discountStr off${if (minOrder > 0) " (min. ₱${String.format("%.0f", minOrder)})" else ""}${if (desc.isNotEmpty()) "\n$desc" else ""}"
                            CouponItem(couponDoc.id, code, discountType, discountValue, minOrder, label)
                        }

                        val labels = items.map { it.label }.toTypedArray()
                        AlertDialog.Builder(this)
                            .setTitle("Select Coupon")
                            .setItems(labels) { _, which ->
                                val coupon = items[which]
                                if (subtotal < coupon.minOrderAmount) {
                                    Toast.makeText(this, "Minimum order of ₱${String.format("%.0f", coupon.minOrderAmount)} required", Toast.LENGTH_LONG).show()
                                    return@setItems
                                }
                                val computedDiscount = if (coupon.discountType == "percentage") {
                                    subtotal * coupon.discountValue / 100.0
                                } else {
                                    coupon.discountValue
                                }
                                discountAmount = if (computedDiscount > subtotal) subtotal else computedDiscount
                                selectedCouponId = coupon.id
                                selectedCouponCode = coupon.code

                                val layoutDiscount = findViewById<LinearLayout>(R.id.layoutDiscount)
                                val txtDiscount = findViewById<TextView>(R.id.txtDiscount)
                                val txtCouponLabel = findViewById<TextView>(R.id.txtCouponLabel)
                                val btnApplyCoupon = findViewById<Button>(R.id.btnApplyCoupon)
                                val txtSubtotal = findViewById<TextView>(R.id.txtSubtotal)
                                val txtTotal = findViewById<TextView>(R.id.txtTotal)

                                layoutDiscount.visibility = View.VISIBLE
                                txtDiscount.text = String.format("- \u20B1%.2f", discountAmount)
                                txtCouponLabel.text = "Coupon: ${coupon.code}"
                                txtCouponLabel.visibility = View.VISIBLE
                                btnApplyCoupon.text = "Change Coupon"
                                refreshTotals(txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
                            }
                            .setNegativeButton("Remove Coupon") { _, _ ->
                                clearCoupon()
                            }
                            .show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Coupon error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
    }

    private fun clearCoupon() {
        selectedCouponId = null
        selectedCouponCode = null
        discountAmount = 0.0
        val layoutDiscount = findViewById<LinearLayout>(R.id.layoutDiscount)
        val txtCouponLabel = findViewById<TextView>(R.id.txtCouponLabel)
        val btnApplyCoupon = findViewById<Button>(R.id.btnApplyCoupon)
        val txtSubtotal = findViewById<TextView>(R.id.txtSubtotal)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)
        layoutDiscount.visibility = View.GONE
        txtCouponLabel.visibility = View.GONE
        btnApplyCoupon.text = "Apply Coupon"
        refreshTotals(txtSubtotal, txtTotal, layoutDiscount, findViewById(R.id.txtDiscount), txtCouponLabel)
    }

    private data class CouponItem(
        val id: String,
        val code: String,
        val discountType: String,
        val discountValue: Double,
        val minOrderAmount: Double,
        val label: String
    )

    @SuppressLint("MissingInflatedId")
    private fun placeOrder(
        checkedRadioId: Int,
        btnPlaceOrder: Button,
        txtSubtotal: TextView,
        txtTotal: TextView,
        layoutDiscount: LinearLayout,
        txtDiscount: TextView,
        txtCouponLabel: TextView
    ) {
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
                    branchId, branchName, paymentMethod, btnPlaceOrder, txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
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
        btnPlaceOrder: Button,
        txtSubtotal: TextView,
        txtTotal: TextView,
        layoutDiscount: LinearLayout,
        txtDiscount: TextView,
        txtCouponLabel: TextView
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
        val total = subtotal + deliveryFee - discountAmount

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

        if (selectedCouponId != null) {
            orderData["couponId"] = selectedCouponId!!
            orderData["couponCode"] = selectedCouponCode!!
            orderData["discountAmount"] = discountAmount
        }

        db.collection("orders")
            .add(orderData)
            .addOnSuccessListener { docRef ->
                if (selectedCouponId != null) {
                    db.collection("coupons").document(selectedCouponId!!)
                        .update("usedCount", FieldValue.increment(1))
                }

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
        val layoutDiscount = findViewById<LinearLayout>(R.id.layoutDiscount)
        val txtDiscount = findViewById<TextView>(R.id.txtDiscount)
        val txtCouponLabel = findViewById<TextView>(R.id.txtCouponLabel)
        updateBagUi(layoutEmpty, recyclerBag, bottomPanel, txtSubtotal, txtTotal, layoutDiscount, txtDiscount, txtCouponLabel)
    }
}
