package com.example.kelompok6_akivili

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeEmailActivity : AppCompatActivity() {

    private lateinit var newEmailEditText: EditText
    private lateinit var changeEmailButton: Button
    private lateinit var generalErrorTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        newEmailEditText = findViewById(R.id.newEmailEditText)
        changeEmailButton = findViewById(R.id.changeEmailButton)
        generalErrorTextView = findViewById(R.id.generalErrorTextView)

        changeEmailButton.setOnClickListener {
            val newEmail = newEmailEditText.text.toString().trim()

            generalErrorTextView.visibility = View.GONE

            if (!isEmailValid(newEmail)) {
                generalErrorTextView.visibility = View.VISIBLE
                generalErrorTextView.text = "*Invalid email"
                return@setOnClickListener
            }

            checkEmailExists(newEmail)
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
                        generalErrorTextView.visibility = View.VISIBLE
                        generalErrorTextView.text = "*Email already exists"
                    } else {
                        updateEmail(email)
                    }
                } else {
                    Log.w(TAG, "checkEmailExists:failure", task.exception)
                    generalErrorTextView.visibility = View.VISIBLE
                    generalErrorTextView.text = "*Error checking email"
                }
            }
    }

    private fun updateEmail(newEmail: String) {
        val user = auth.currentUser
        user?.updateEmail(newEmail)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    db.collection("users").document(user.uid)
                        .update("email", newEmail)
                        .addOnCompleteListener { firestoreTask ->
                            if (firestoreTask.isSuccessful) {
                                Toast.makeText(this, "Email berhasil diubah", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                generalErrorTextView.visibility = View.VISIBLE
                                generalErrorTextView.text = "*Failed to update email in Firestore"
                            }
                        }
                } else {
                    Log.w(TAG, "updateEmail:failure", task.exception)
                    generalErrorTextView.visibility = View.VISIBLE
                    generalErrorTextView.text = "*Failed to update email"
                }
            }
    }

    companion object {
        private const val TAG = "ChangeEmailActivity"
    }
}
