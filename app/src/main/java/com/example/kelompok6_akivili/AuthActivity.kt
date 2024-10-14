package com.example.kelompok6_akivili

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            registerUser(email, password, name)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(email, password)
        }
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
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error writing user data", e)
                        }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                }
            }
    }

    companion object {
        private const val TAG = "AuthActivity"
    }
}
