package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialise Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        val btnBack       = findViewById<ImageView>(R.id.btnBack)
        val etFullName    = findViewById<android.widget.EditText>(R.id.etFullName)
        val etEmail       = findViewById<android.widget.EditText>(R.id.etEmail)
        val etPassword    = findViewById<android.widget.EditText>(R.id.etPassword)
        val etConfirmPass = findViewById<android.widget.EditText>(R.id.etConfirmPassword)
        val btnSignUp     = findViewById<Button>(R.id.btnSignUp)
        val txtError      = findViewById<TextView>(R.id.txtError)
        val txtGoToLogin  = findViewById<TextView>(R.id.txtGoToLogin)

        // Back button — return to login screen
        btnBack.setOnClickListener {
            finish()
        }

        // "Log In" link — return to login screen
        txtGoToLogin.setOnClickListener {
            finish()
        }

        // Handle Sign Up button click
        btnSignUp.setOnClickListener {
            val fullName    = etFullName.text.toString().trim()
            val email       = etEmail.text.toString().trim()
            val password    = etPassword.text.toString().trim()
            val confirmPass = etConfirmPass.text.toString().trim()

            // Basic client-side validation
            if (fullName.isEmpty()) {
                showError(txtError, "Please enter your full name.")
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                showError(txtError, "Please enter your email.")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                showError(txtError, "Please enter a password.")
                return@setOnClickListener
            }
            if (password.length < 6) {
                showError(txtError, "Password must be at least 6 characters.")
                return@setOnClickListener
            }
            if (password != confirmPass) {
                showError(txtError, "Passwords do not match.")
                return@setOnClickListener
            }

            // Disable button while request is in flight
            btnSignUp.isEnabled = false
            txtError.visibility = View.GONE

            // Step 1: Create the Firebase Auth account
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser!!

                        // Step 2: Save display name to the Auth profile
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()

                        user.updateProfile(profileUpdates).addOnCompleteListener {

                            // Step 3: Save user document to Firestore "users" collection
                            val userDoc = hashMapOf(
                                "uid"       to user.uid,
                                "name"      to fullName,
                                "email"     to email,
                                "role"      to "customer",
                                "createdAt" to com.google.firebase.Timestamp.now()
                            )

                            db.collection("users")
                                .document(user.uid)
                                .set(userDoc)
                                .addOnSuccessListener {
                                    auth.signOut()
                                    Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    auth.signOut()
                                    Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                        }

                    } else {
                        btnSignUp.isEnabled = true
                        val errorMsg = task.exception?.message ?: "Sign up failed. Please try again."
                        showError(txtError, errorMsg)
                    }
                }
        }
    }

    // Helper to display the inline error message
    private fun showError(txtError: TextView, message: String) {
        txtError.text = message
        txtError.visibility = View.VISIBLE
    }
}
