package com.example.kelompok6_akivili.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

object FirestoreUtils {

    private val db = FirebaseFirestore.getInstance()

    fun saveUserData(userId: String, email: String, displayName: String, gameName: String, itemName: String, amount: Int, paymentMethod: String) {
        val user = hashMapOf(
            "email" to email,
            "displayName" to displayName,
            "gameName" to gameName,
            "itemName" to itemName,
            "totalAmount" to amount,
            "paymentMethod" to paymentMethod,
            "status" to "Pending",
            "qrCodeUrl" to "",
            "receiptUrl" to "",
            "orderNumber" to generateOrderNumber(),
            "createdAt" to getCurrentDate(),
            "lastLogin" to getCurrentDate()
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                println("Data pengguna berhasil disimpan!")
            }
            .addOnFailureListener { e ->
                println("Error menyimpan data pengguna: $e")
            }
    }

    fun updatePaymentStatus(userId: String, status: String, qrCodeUrl: String) {
        db.collection("users").document(userId)
            .update(
                "status", status,
                "qrCodeUrl", qrCodeUrl
            )
            .addOnSuccessListener {
                println("Status pembayaran berhasil diperbarui!")
            }
            .addOnFailureListener { e ->
                println("Error memperbarui status: $e")
            }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun generateOrderNumber(): String {
        return "INV" + System.currentTimeMillis().toString().takeLast(8)
    }
}
