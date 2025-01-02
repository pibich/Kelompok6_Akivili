package com.example.kelompok6_akivili

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi Firebase App Check
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Cek dan minta izin lokasi
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getLastLocation()
        }
    }

    // Mengambil lokasi terakhir
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    saveLocationToFirestore(location.latitude, location.longitude)
                } else {
                    Log.w(TAG, "Gagal mendapatkan lokasi terakhir.")
                }
            }.addOnFailureListener {
                Log.e(TAG, "Gagal mendapatkan lokasi: ${it.message}")
            }
        }
    }

    // Menyimpan lokasi ke Firestore
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
                    Log.d(TAG, "Lokasi berhasil disimpan!")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Gagal menyimpan lokasi", e)
                }
        } else {
            Log.w(TAG, "User belum login.")
        }
    }

    // Menangani hasil permintaan izin
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Log.w(TAG, "Izin lokasi ditolak.")
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TAG = "MainActivity"
    }
}
