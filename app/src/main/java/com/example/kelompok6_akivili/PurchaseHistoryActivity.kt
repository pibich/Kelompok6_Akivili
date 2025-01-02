package com.example.kelompok6_akivili

import PurchaseHistoryManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class PurchaseHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PurchaseHistoryAdapter
    private lateinit var historyManager: PurchaseHistoryManager
    private lateinit var emptyView: TextView
    private lateinit var clearHistoryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Inisialisasi UI
        recyclerView = findViewById(R.id.recyclerViewHistory)
        emptyView = findViewById(R.id.emptyView)  // TextView untuk tampilan kosong
        clearHistoryButton = findViewById(R.id.clearHistoryButton)  // Tombol hapus riwayat

        historyManager = PurchaseHistoryManager(this)

        // Memuat dan Menampilkan Riwayat Transaksi
        loadHistory()

        // Tombol Hapus Riwayat
        clearHistoryButton.setOnClickListener {
            showClearConfirmationDialog()
        }

        // Bottom Navigation Handler
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_profile

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

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadHistory() {
        val purchaseList = historyManager.getAllPurchases().toMutableList()

        if (purchaseList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE

            // Atur adapter dan RecyclerView
            adapter = PurchaseHistoryAdapter(this, purchaseList)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)
        }
    }

    // Tampilkan dialog konfirmasi sebelum menghapus riwayat
    private fun showClearConfirmationDialog() {
        val purchaseList = historyManager.getAllPurchases() // Ambil semua riwayat transaksi

        if (purchaseList.isEmpty()) {
            // Tampilkan pesan jika tidak ada riwayat pembelian
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Tidak Ada Riwayat")
            builder.setMessage("Tidak ada riwayat pembelian yang dapat dihapus.")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        } else {
            // Tampilkan konfirmasi jika ada riwayat
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Hapus Riwayat")
            builder.setMessage("Apakah Anda yakin ingin menghapus semua riwayat pembelian?")
            builder.setPositiveButton("Ya") { dialog, _ ->
                historyManager.clearHistory()
                loadHistory()
                dialog.dismiss()
            }
            builder.setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }
}
