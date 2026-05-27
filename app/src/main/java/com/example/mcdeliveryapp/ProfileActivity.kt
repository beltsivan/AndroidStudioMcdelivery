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
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.EmailAuthProvider
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

        val cardEditProfile = findViewById<MaterialCardView>(R.id.cardEditProfile)
        val cardEditAddress = findViewById<MaterialCardView>(R.id.cardEditAddress)
        val cardChangePassword = findViewById<MaterialCardView>(R.id.cardChangePassword)

        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val btnEditAddress = findViewById<Button>(R.id.btnEditAddress)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)
        val txtProfileStatus = findViewById<TextView>(R.id.txtProfileStatus)

        val etStreet = findViewById<EditText>(R.id.etStreet)
        val etBarangay = findViewById<EditText>(R.id.etBarangay)
        val etMunicipality = findViewById<EditText>(R.id.etMunicipality)
        val etProvince = findViewById<EditText>(R.id.etProvince)
        val etPostalCode = findViewById<EditText>(R.id.etPostalCode)
        val btnSaveAddress = findViewById<Button>(R.id.btnSaveAddress)
        val txtAddressStatus = findViewById<TextView>(R.id.txtAddressStatus)

        val txtDisplayName = findViewById<TextView>(R.id.txtDisplayName)
        val etCurrentPassword = findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSavePassword = findViewById<Button>(R.id.btnSavePassword)
        val txtPasswordStatus = findViewById<TextView>(R.id.txtPasswordStatus)

        fun hideAllSections() {
            cardEditProfile.visibility = View.GONE
            cardEditAddress.visibility = View.GONE
            cardChangePassword.visibility = View.GONE
            txtProfileStatus.visibility = View.GONE
            txtAddressStatus.visibility = View.GONE
            txtPasswordStatus.visibility = View.GONE
        }

        btnEditProfile.setOnClickListener {
            hideAllSections()
            cardEditProfile.visibility = View.VISIBLE
        }

        btnEditAddress.setOnClickListener {
            hideAllSections()
            cardEditAddress.visibility = View.VISIBLE
        }

        btnChangePassword.setOnClickListener {
            hideAllSections()
            cardChangePassword.visibility = View.VISIBLE
        }

        val user = auth.currentUser
        if (user != null) {
            etEmail.setText(user.email)

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) return@addOnSuccessListener

                    etFullName.setText(doc.getString("fullName") ?: doc.getString("name") ?: "")
                    txtDisplayName.text = doc.getString("fullName") ?: doc.getString("name") ?: ""

                    etPhone.setText(doc.getString("phoneNumber") ?: doc.getString("contactNumber") ?: "")

                    val address = doc.get("address") as? Map<*, *>
                    if (address != null) {
                        etStreet.setText(address["street"] as? String ?: "")
                        etBarangay.setText(address["barangay"] as? String ?: "")
                        etMunicipality.setText(address["municipality"] as? String ?: "")
                        etProvince.setText(address["province"] as? String ?: "")
                        etPostalCode.setText(address["postalCode"] as? String ?: "")
                    }
                }

            btnSaveProfile.setOnClickListener {
                val name = etFullName.text.toString().trim()
                val phone = etPhone.text.toString().trim()

                if (name.isEmpty()) {
                    showStatus(txtProfileStatus, "Please enter your full name", "#D32F2F")
                    return@setOnClickListener
                }
                if (phone.isEmpty() || !phone.matches(Regex("^[0-9]{11}$"))) {
                    showStatus(txtProfileStatus, "Phone number must be exactly 11 digits", "#D32F2F")
                    return@setOnClickListener
                }

                btnSaveProfile.isEnabled = false
                txtProfileStatus.visibility = View.GONE

                val updates = hashMapOf<String, Any>(
                    "fullName" to name,
                    "phoneNumber" to phone
                )
                db.collection("users").document(user.uid)
                    .set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        showStatus(txtProfileStatus, "Profile saved!", "#2E7D32")
                        txtDisplayName.text = name
                        btnSaveProfile.isEnabled = true
                    }
                    .addOnFailureListener { e ->
                        showStatus(txtProfileStatus, "Error: ${e.message}", "#D32F2F")
                        btnSaveProfile.isEnabled = true
                    }
            }

            btnSaveAddress.setOnClickListener {
                val street = etStreet.text.toString().trim()
                val barangay = etBarangay.text.toString().trim()
                val municipality = etMunicipality.text.toString().trim()
                val province = etProvince.text.toString().trim()
                val postalCode = etPostalCode.text.toString().trim()

                if (street.isEmpty() || barangay.isEmpty() || municipality.isEmpty() ||
                    province.isEmpty() || postalCode.isEmpty()
                ) {
                    showStatus(txtAddressStatus, "Please fill in all address fields", "#D32F2F")
                    return@setOnClickListener
                }

                btnSaveAddress.isEnabled = false
                txtAddressStatus.visibility = View.GONE

                val addressData = hashMapOf<String, Any>(
                    "street" to street,
                    "barangay" to barangay,
                    "municipality" to municipality,
                    "province" to province,
                    "postalCode" to postalCode
                )
                db.collection("users").document(user.uid)
                    .set(hashMapOf("address" to addressData), com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        showStatus(txtAddressStatus, "Address saved!", "#2E7D32")
                        btnSaveAddress.isEnabled = true
                    }
                    .addOnFailureListener { e ->
                        showStatus(txtAddressStatus, "Error: ${e.message}", "#D32F2F")
                        btnSaveAddress.isEnabled = true
                    }
            }

            btnSavePassword.setOnClickListener {
                val currentPw = etCurrentPassword.text.toString()
                val newPw = etNewPassword.text.toString()
                val confirmPw = etConfirmPassword.text.toString()

                if (currentPw.isEmpty()) {
                    showStatus(txtPasswordStatus, "Enter current password", "#D32F2F")
                    return@setOnClickListener
                }
                if (newPw.isEmpty() || newPw.length < 6) {
                    showStatus(txtPasswordStatus, "New password must be at least 6 characters", "#D32F2F")
                    return@setOnClickListener
                }
                if (newPw != confirmPw) {
                    showStatus(txtPasswordStatus, "Passwords do not match", "#D32F2F")
                    return@setOnClickListener
                }

                btnSavePassword.isEnabled = false
                txtPasswordStatus.visibility = View.GONE

                val credential = EmailAuthProvider.getCredential(user.email!!, currentPw)
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(newPw)
                            .addOnSuccessListener {
                                showStatus(txtPasswordStatus, "Password changed!", "#2E7D32")
                                etCurrentPassword.text.clear()
                                etNewPassword.text.clear()
                                etConfirmPassword.text.clear()
                                btnSavePassword.isEnabled = true
                            }
                            .addOnFailureListener { e ->
                                showStatus(txtPasswordStatus, "Error: ${e.message}", "#D32F2F")
                                btnSavePassword.isEnabled = true
                            }
                    }
                    .addOnFailureListener {
                        showStatus(txtPasswordStatus, "Current password is incorrect", "#D32F2F")
                        btnSavePassword.isEnabled = true
                    }
            }
        } else {
            etEmail.setText("Not signed in")
            btnSaveProfile.isEnabled = false
            btnSaveAddress.isEnabled = false
            btnSavePassword.isEnabled = false
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
        findViewById<LinearLayout>(R.id.navMore).setOnClickListener { }
    }
}
