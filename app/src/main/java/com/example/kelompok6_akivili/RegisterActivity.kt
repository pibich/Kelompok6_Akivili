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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var generalErrorTextView: TextView
    private lateinit var loginTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        registerButton = findViewById(R.id.registerButton)
        generalErrorTextView = findViewById(R.id.generalErrorTextView)
        loginTextView = findViewById(R.id.loginTextView)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()

            generalErrorTextView.visibility = View.GONE

            if (!isEmailValid(email)) {
                generalErrorTextView.visibility = View.VISIBLE
                generalErrorTextView.text = "*Invalid email, please use another email"
                return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                generalErrorTextView.visibility = View.VISIBLE
                generalErrorTextView.text = "*Password must be at least 8 characters and contain letters and numbers"
                return@setOnClickListener
            }

            registerUser(email, password, name)
        }

        val loginFullText = "Already have an account? Login here"
        val loginSpannable = SpannableString(loginFullText)

        val loginClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val loginColor = ContextCompat.getColor(this, R.color.redlink)
        val loginStartIndex = loginFullText.indexOf("Login")
        val loginEndIndex = loginStartIndex + "Login".length

        loginSpannable.setSpan(loginClickableSpan, loginStartIndex, loginEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginSpannable.setSpan(ForegroundColorSpan(loginColor), loginStartIndex, loginEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        loginTextView.text = loginSpannable
        loginTextView.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() }
    }

    private fun registerUser(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userData = hashMapOf(
                        "uid" to user?.uid,
                        "email" to email,
                        "name" to name
                    )

                    db.collection("users").document(user!!.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d(TAG, "User data successfully written!")
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error writing user data", e)
                            Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    if (task.exception is FirebaseAuthInvalidUserException) {
                        generalErrorTextView.visibility = View.VISIBLE
                        generalErrorTextView.text = "*Email is already in use"
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        generalErrorTextView.visibility = View.VISIBLE
                        generalErrorTextView.text = "*Registration failed: ${task.exception?.message}"
                    }
                }
            }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}
