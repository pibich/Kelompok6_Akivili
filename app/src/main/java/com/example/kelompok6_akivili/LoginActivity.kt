package com.example.kelompok6_akivili

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(email, password)
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                }
            }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
