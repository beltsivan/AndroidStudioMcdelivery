package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialise Firebase Auth
        auth = FirebaseAuth.getInstance()

        val etEmail     = findViewById<android.widget.EditText>(R.id.etEmail)
        val etPassword  = findViewById<android.widget.EditText>(R.id.etPassword)
        val btnLogin    = findViewById<Button>(R.id.btnLogin)
        val txtError    = findViewById<TextView>(R.id.txtError)
        val txtGoToSignUp = findViewById<TextView>(R.id.txtGoToSignUp)

        // Navigate to Sign Up screen
        txtGoToSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Handle Login button click
        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Basic client-side validation
            if (email.isEmpty()) {
                showError(txtError, "Please enter your email.")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                showError(txtError, "Please enter your password.")
                return@setOnClickListener
            }

            // Disable the button while request is in flight
            btnLogin.isEnabled = false
            txtError.visibility = View.GONE

            // Firebase sign-in
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    btnLogin.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                        // Go to main app screen and clear back-stack
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        val errorMsg = task.exception?.message ?: "Login failed. Please try again."
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
