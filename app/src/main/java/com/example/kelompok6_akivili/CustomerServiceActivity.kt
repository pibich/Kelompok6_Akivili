package com.example.kelompok6_akivili

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class CustomerServiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_service)

        val contactButton1: Button = findViewById(R.id.contactButton1)
        val contactButton2: Button = findViewById(R.id.contactButton2)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Handle button klik untuk membuka WhatsApp
        contactButton1.setOnClickListener {
            openWhatsApp("6285920637925") // Ganti dengan nomor CS 1
        }

        contactButton2.setOnClickListener {
            openWhatsApp("6289503215008") // Ganti dengan nomor CS 2
        }

        findViewById<Button>(R.id.contactButton3).setOnClickListener {
            openWhatsApp("6282179797548")  // Ganti dengan nomor CS 3
        }

        // Bottom Navigation Handler
        bottomNavigationView.selectedItemId = R.id.nav_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun openWhatsApp(phoneNumber: String) {
        val uri = Uri.parse("https://wa.me/$phoneNumber")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}
