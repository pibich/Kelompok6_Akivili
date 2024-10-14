package com.example.kelompok6_akivili

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            registerUser(email, password, name)
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
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}
