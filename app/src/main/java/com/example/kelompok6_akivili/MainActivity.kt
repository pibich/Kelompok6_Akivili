package com.example.kelompok6_akivili

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    saveLocationToFirestore(latitude, longitude)
                }
            }
        }
    }

    private fun saveLocationToFirestore(latitude: Double, longitude: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val locationData = hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "timestamp" to System.currentTimeMillis()
            )

            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("locationHistory")
                .add(locationData)
                .addOnSuccessListener {
                    Log.d(TAG, "Location successfully saved!")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error saving location", e)
                }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                // Tampilin pesan kalau ditolak
            }
        }
    }



    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
