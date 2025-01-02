package com.example.kelompok6_akivili

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetButton: Button
    private lateinit var generalErrorTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        emailEditText = findViewById(R.id.emailEditText)
        resetButton = findViewById(R.id.resetButton)
        generalErrorTextView = findViewById(R.id.generalErrorTextView)

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            generalErrorTextView.visibility = View.GONE

            if (!isEmailValid(email)) {
                generalErrorTextView.visibility = View.VISIBLE
                generalErrorTextView.text = "*Invalid email"
                return@setOnClickListener
            }

            checkEmailExists(email)
        }

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkEmailExists(email: String) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null && !task.result.isEmpty) {
                        sendResetPasswordEmail(email)
                    } else {
                        generalErrorTextView.visibility = View.VISIBLE
                        generalErrorTextView.text = "*Email not found"
                    }
                } else {
                    Log.w(TAG, "checkEmailExists:failure", task.exception)
                    generalErrorTextView.visibility = View.VISIBLE
                    generalErrorTextView.text = "*Error checking email"
                }
            }
    }

    private fun sendResetPasswordEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset password email sent successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.w(TAG, "sendPasswordResetEmail:failure", task.exception)
                    generalErrorTextView.visibility = View.VISIBLE
                    generalErrorTextView.text = "*Failed to send reset email"
                }
            }
    }

    companion object {
        private const val TAG = "ResetPasswordActivity"
    }
}
