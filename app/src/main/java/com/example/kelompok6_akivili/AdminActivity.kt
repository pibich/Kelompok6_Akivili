package com.example.kelompok6_akivili

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {

    private lateinit var menuViewPager: LinearLayout
    private lateinit var menuGame: LinearLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var textViewPager: TextView
    private lateinit var textGame: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })

        // Initialize views
        menuViewPager = findViewById(R.id.menuViewPager)
        menuGame = findViewById(R.id.menuGame)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        textViewPager = findViewById(R.id.textViewPager)
        textGame = findViewById(R.id.textGame)

        menuViewPager.setOnClickListener {
            // Handle navigation for "Kelola View Pager"
            startActivity(Intent(this, ViewPagerManagementActivity::class.java))
        }

        menuGame.setOnClickListener {
            // Handle navigation for "Kelola Game"
            startActivity(Intent(this, GameEditActivity::class.java))
        }

        // Set the Bottom Navigation Menu
        bottomNavigationView.selectedItemId = R.id.nav_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }

                R.id.nav_profile -> {
                    // Navigate to the ProfileActivity
                    startActivity(Intent(this, ProfileAdminActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Keluar")
        builder.setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
        builder.setPositiveButton("Ya") { _, _ -> finish() }
        builder.setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}
