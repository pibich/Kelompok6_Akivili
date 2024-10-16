package com.example.kelompok6_akivili

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore

class ChangeEmailActivity : AppCompatActivity() {

    private lateinit var newEmailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var changeEmailButton: Button
    private lateinit var generalErrorTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        // Inisialisasi Firebase Auth dan Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inisialisasi komponen UI
        newEmailEditText = findViewById(R.id.newEmailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        changeEmailButton = findViewById(R.id.changeEmailButton)
        generalErrorTextView = findViewById(R.id.generalErrorTextView)

        // Aksi saat tombol change email ditekan
        changeEmailButton.setOnClickListener {
            val newEmail = newEmailEditText.text.toString().trim()
            val currentPassword = passwordEditText.text.toString().trim()

            generalErrorTextView.visibility = View.GONE

            // Validasi input email
            if (!isEmailValid(newEmail)) {
                generalErrorTextView.visibility = View.VISIBLE
                generalErrorTextView.text = "*Email tidak valid"
                return@setOnClickListener
            }

            // Validasi input password
            if (currentPassword.isEmpty()) {
                generalErrorTextView.visibility = View.VISIBLE
                generalErrorTextView.text = "*Masukkan kata sandi saat ini"
                return@setOnClickListener
            }

            // Cek apakah email baru sudah terdaftar
            checkEmailExists(newEmail, currentPassword)
        }
    }

    // Fungsi untuk validasi email
    private fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Fungsi untuk mengecek apakah email baru sudah ada di database Firestore
    private fun checkEmailExists(newEmail: String, currentPassword: String) {
        db.collection("users")
            .whereEqualTo("email", newEmail)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null && !task.result.isEmpty) {
                        generalErrorTextView.visibility = View.VISIBLE
                        generalErrorTextView.text = "*Email sudah terdaftar"
                    } else {
                        // Jika email tidak ada, lanjut ke proses re-authentication dan update email
                        reauthenticateAndChangeEmail(newEmail, currentPassword)
                    }
                } else {
                    Log.w(TAG, "checkEmailExists:failure", task.exception)
                    generalErrorTextView.visibility = View.VISIBLE
                    generalErrorTextView.text = "*Error saat memeriksa email"
                }
            }
    }

    // Fungsi untuk re-authenticate pengguna dan mengubah email
    private fun reauthenticateAndChangeEmail(newEmail: String, currentPassword: String) {
        val user = auth.currentUser
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Jika re-authentication berhasil, update email pengguna
                        user.updateEmail(newEmail)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Update email di Firestore setelah sukses update di Firebase Auth
                                    db.collection("users").document(user.uid)
                                        .update("email", newEmail)
                                        .addOnCompleteListener { firestoreTask ->
                                            if (firestoreTask.isSuccessful) {
                                                Toast.makeText(this, "Email berhasil diubah", Toast.LENGTH_SHORT).show()
                                                finish()
                                            } else {
                                                generalErrorTextView.visibility = View.VISIBLE
                                                generalErrorTextView.text = "*Gagal memperbarui email di Firestore"
                                            }
                                        }
                                } else {
                                    Log.w(TAG, "updateEmail:failure", updateTask.exception)
                                    generalErrorTextView.visibility = View.VISIBLE
                                    generalErrorTextView.text = "*Gagal memperbarui email"
                                }
                            }
                    } else {
                        // Menangani error jika re-authentication gagal
                        reauthTask.exception?.let {
                            if (it is FirebaseAuthInvalidCredentialsException) {
                                generalErrorTextView.visibility = View.VISIBLE
                                generalErrorTextView.text = "*Kata sandi salah"
                            } else {
                                generalErrorTextView.visibility = View.VISIBLE
                                generalErrorTextView.text = "*Gagal re-autentikasi: ${it.message}"
                            }
                        }
                    }
                }
        }
    }

    companion object {
        private const val TAG = "ChangeEmailActivity"
    }
}
