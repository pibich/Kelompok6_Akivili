package com.example.kelompok6_akivili

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ProfileAdminActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var headerTitle: TextView
    private lateinit var profileName: TextView
    private lateinit var logoutButton: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_admin)

        // Initialize views
        backButton = findViewById(R.id.backButton)
        headerTitle = findViewById(R.id.headerTitle)
        profileName = findViewById(R.id.profileName)
        logoutButton = findViewById(R.id.logoutButton)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Set bottom navigation
        bottomNavigationView.selectedItemId = R.id.nav_profile
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }

        // Back button functionality
        val backButton: ImageView = findViewById(R.id.backButton)
        val currentActivity = this::class.java.simpleName

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Logout functionality
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
