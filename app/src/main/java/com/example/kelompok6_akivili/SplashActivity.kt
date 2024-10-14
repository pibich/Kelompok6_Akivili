package com.example.kelompok6_akivili

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(fadeIn)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navigateToLogin()
            } else {
                navigateToLogin()
            }
        }
    }
}
