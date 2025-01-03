package com.example.kelompok6_akivili

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var generalErrorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
        registerTextView = findViewById(R.id.registerTextView)
        generalErrorTextView = findViewById(R.id.generalErrorTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            generalErrorTextView.visibility = View.GONE

            if (email.isEmpty()) {
                generalErrorTextView.visibility = View.VISIBLE
                generalErrorTextView.text = "*Email must be filled"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                generalErrorTextView.visibility = View.VISIBLE
                generalErrorTextView.text = "*Password must be filled"
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        val registerFullText = "Don't have an account? Register here"
        val registerSpannable = SpannableString(registerFullText)

        val registerClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }

        val registerColor = ContextCompat.getColor(this, R.color.redlink)
        val registerStartIndex = registerFullText.indexOf("Register")
        val registerEndIndex = registerStartIndex + "Register".length

        registerSpannable.setSpan(registerClickableSpan, registerStartIndex, registerEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        registerSpannable.setSpan(ForegroundColorSpan(registerColor), registerStartIndex, registerEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        registerTextView.text = registerSpannable
        registerTextView.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        val forgotPasswordFullText = "Forgot Password?"
        val forgotPasswordSpannable = SpannableString(forgotPasswordFullText)

        val forgotPasswordClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java))
            }
        }

        val forgotPasswordColor = ContextCompat.getColor(this, R.color.redlink)
        forgotPasswordSpannable.setSpan(forgotPasswordClickableSpan, 0, forgotPasswordFullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        forgotPasswordSpannable.setSpan(ForegroundColorSpan(forgotPasswordColor), 0, forgotPasswordFullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        forgotPasswordTextView.text = forgotPasswordSpannable
        forgotPasswordTextView.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }

    private fun loginUser(email: String, password: String) {
        // Lakukan login menggunakan Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                generalErrorTextView.visibility = View.GONE

                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser

                    // Langsung periksa apakah pengguna adalah admin
                    if (user != null) {
                        checkIfAdmin(user.email ?: "")
                    }
                } else {
                    // Login gagal, tampilkan pesan kesalahan
                    val exception = task.exception
                    Log.e(TAG, "Login failed: ${exception?.message}")
                    generalErrorTextView.visibility = View.VISIBLE
                    generalErrorTextView.text = "*Login failed. Check your email or password."
                }
            }
    }

    private fun checkIfAdmin(email: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Mengakses data admin dari Firestore
        firestore.collection("users")
            .document("Admin")  // Pastikan Anda mengakses dokumen admin yang benar
            .get()
            .addOnSuccessListener { document ->
                val adminEmail = document.getString("email")

                if (adminEmail == email) {
                    // Jika email pengguna cocok dengan admin, arahkan ke AdminActivity
                    startActivity(Intent(this, AdminActivity::class.java))
                    finish()
                } else {
                    // Jika bukan admin, arahkan ke HomeActivity
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching admin data: $exception")
                Toast.makeText(this, "Failed to verify admin status.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification email successfully sent to ${user.email}.", Toast.LENGTH_LONG).show()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
