package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val txtEmail = findViewById<TextView>(R.id.txtEmail)
        val etStreet = findViewById<EditText>(R.id.etStreet)
        val etBarangay = findViewById<EditText>(R.id.etBarangay)
        val etMunicipality = findViewById<EditText>(R.id.etMunicipality)
        val etProvince = findViewById<EditText>(R.id.etProvince)
        val etPostalCode = findViewById<EditText>(R.id.etPostalCode)
        val etContactNumber = findViewById<EditText>(R.id.etContactNumber)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)

        val user = auth.currentUser
        if (user != null) {
            txtEmail.text = user.email

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) return@addOnSuccessListener

                    val address = doc.get("address") as? Map<*, *>
                    if (address != null) {
                        etStreet.setText(address["street"] as? String ?: "")
                        etBarangay.setText(address["barangay"] as? String ?: "")
                        etMunicipality.setText(address["municipality"] as? String ?: "")
                        etProvince.setText(address["province"] as? String ?: "")
                        etPostalCode.setText(address["postalCode"] as? String ?: "")
                    }
                    etContactNumber.setText(doc.getString("contactNumber") ?: "")
                }
        } else {
            txtEmail.text = "Not signed in"
            btnSave.isEnabled = false
        }

        btnSave.setOnClickListener {
            val street = etStreet.text.toString().trim()
            val barangay = etBarangay.text.toString().trim()
            val municipality = etMunicipality.text.toString().trim()
            val province = etProvince.text.toString().trim()
            val postalCode = etPostalCode.text.toString().trim()
            val contactNumber = etContactNumber.text.toString().trim()

            if (street.isEmpty() || barangay.isEmpty() || municipality.isEmpty() ||
                province.isEmpty() || postalCode.isEmpty()
            ) {
                showStatus(txtStatus, "Please fill in all address fields", "#D32F2F")
                return@setOnClickListener
            }
            if (contactNumber.isEmpty()) {
                showStatus(txtStatus, "Please enter your contact number", "#D32F2F")
                return@setOnClickListener
            }
            if (!contactNumber.matches(Regex("^[0-9]{11}$"))) {
                showStatus(txtStatus, "Contact number must be exactly 11 digits", "#D32F2F")
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            txtStatus.visibility = View.GONE

            val addressData = hashMapOf<String, Any>(
                "street" to street,
                "barangay" to barangay,
                "municipality" to municipality,
                "province" to province,
                "postalCode" to postalCode
            )

            val updates = hashMapOf<String, Any>(
                "address" to addressData,
                "contactNumber" to contactNumber
            )

            db.collection("users")
                .document(user!!.uid)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    showStatus(txtStatus, "Profile saved successfully!", "#2E7D32")
                    btnSave.isEnabled = true
                }
                .addOnFailureListener { e ->
                    showStatus(txtStatus, "Error: ${e.message}", "#D32F2F")
                    btnSave.isEnabled = true
                }
        }

        setupBottomNav()
    }

    private fun showStatus(view: TextView, message: String, color: String) {
        view.text = message
        view.setTextColor(android.graphics.Color.parseColor(color))
        view.visibility = View.VISIBLE
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
        findViewById<LinearLayout>(R.id.navCoupons).setOnClickListener {
            startActivity(Intent(this, CouponsActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navMore).setOnClickListener { /* active tab */ }
    }
}
