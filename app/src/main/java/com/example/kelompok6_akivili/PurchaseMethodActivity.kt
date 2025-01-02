package com.example.kelompok6_akivili

import CartItem
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.UUID
import kotlin.properties.Delegates

class PurchaseMethodActivity : AppCompatActivity() {

    private var selectedPaymentMethod: String? = null
    private var totalPurchase: Int = 0
    private lateinit var userEmail: String
    private lateinit var email: String
    private lateinit var gameName: String
    private lateinit var nominal: String
    private var price by Delegates.notNull<Int>()
    private lateinit var gameNames: String
    private lateinit var itemNames: String
    private lateinit var totalPrices: IntArray  // Array untuk harga total per item
    private lateinit var quantities: IntArray  // Array untuk kuantitas item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_method)

        val paymentGrid: GridLayout = findViewById(R.id.paymentGrid)
        val totalPurchaseTextView: TextView = findViewById(R.id.totalPurchase)  // Tampilkan total pembelian
        val buyButton: MaterialButton = findViewById(R.id.buyButton)

        // Ambil data dari Intent
        userEmail = intent.getStringExtra("userEmail") ?: ""
        email = intent.getStringExtra("email") ?: ""
        gameName = intent.getStringExtra("gameName") ?: "Tidak diketahui"
        nominal = intent.getStringExtra("nominal") ?: "Tidak diketahui"
        price = intent.getIntExtra("price", 0) // Mengambil harga dengan nilai default 0 jika tidak ada data
        gameNames = intent.getStringExtra("gameNames") ?: "Tidak diketahui"
        itemNames = intent.getStringExtra("itemNames") ?: "Item Tidak Diketahui"
        totalPrices = intent.getIntArrayExtra("totalPrices") ?: IntArray(0)
        quantities = intent.getIntArrayExtra("quantities") ?: IntArray(0)
        totalPurchase = totalPrices.sum()

        // Log untuk memverifikasi penerimaan data
        Log.d("PurchaseMethodActivity", "Received data - email: $email, gameName: $gameName, itemName: $nominal, price: $price")

        val isBuyNow = intent.getBooleanExtra("isBuyNow", false) // Flag untuk mengetahui beli langsung atau dari keranjang

        // Tampilkan harga total
        if (isBuyNow) {
            totalPurchaseTextView.text = "Total pembelian\n${formatPrice(price)}"
        } else {
            totalPurchaseTextView.text = "Total pembelian\n${formatPrice(totalPurchase)}"
        }

        // Nonaktifkan tombol beli di awal
        buyButton.isEnabled = false

        // Setup grid metode pembayaran
        setupPaymentGrid(paymentGrid, buyButton)

        // Handle pembelian langsung
        if (isBuyNow) {
            buyButton.setOnClickListener {
                if (selectedPaymentMethod != null) {
                    val intent = Intent(this, PurchaseActivity::class.java).apply {
                        putExtra("userEmail", email)
                        putExtra("gameName", gameName)
                        putExtra("nominal", nominal)
                        putExtra("paymentMethod", selectedPaymentMethod)
                        putExtra("price", price)
                        putExtra("orderNumber", generateOrderNumber())
                        putExtra("isBuyNow", true)  // Flag untuk pembelian langsung
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Silakan pilih metode pembayaran terlebih dahulu.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Handle pembelian dari keranjang
            buyButton.setOnClickListener {
                if (selectedPaymentMethod != null) {
                    val intent = Intent(this, PurchaseActivity::class.java).apply {
                        putExtra("userEmail", userEmail)
                        putExtra("gameNames", gameNames)
                        putExtra("itemNames", itemNames)
                        putExtra("paymentMethod", selectedPaymentMethod)
                        putExtra("totalPurchase", totalPurchase)
                        putExtra("orderNumber", generateOrderNumber())
                        putExtra("totalPrices", totalPrices)
                        putExtra("quantities", quantities)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Silakan pilih metode pembayaran terlebih dahulu.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Navigasi ke halaman lain
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = 0
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Tombol kembali
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // Setup grid untuk memilih metode pembayaran
    private fun setupPaymentGrid(paymentGrid: GridLayout, buyButton: MaterialButton) {
        for (i in 0 until paymentGrid.childCount) {
            val linearLayout = paymentGrid.getChildAt(i) as? LinearLayout
            val textView = linearLayout?.findViewWithTag<TextView>("paymentText")

            linearLayout?.setOnClickListener {
                selectedPaymentMethod = textView?.text.toString()
                buyButton.isEnabled = true

                // Reset opacity semua tombol
                for (j in 0 until paymentGrid.childCount) {
                    val child = paymentGrid.getChildAt(j) as? LinearLayout
                    child?.alpha = 1.0f
                }
                // Set opacity untuk tombol yang dipilih
                linearLayout.alpha = 0.5f
                Toast.makeText(this, "Metode pembayaran dipilih: $selectedPaymentMethod", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menghasilkan nomor pesanan
    private fun generateOrderNumber(): String {
        return "INV" + System.currentTimeMillis().toString().takeLast(8)
    }

    // Function to format price with "Rp" and thousand separators
    private fun formatPrice(price: Int): String {
        val decimalFormat = java.text.DecimalFormat("#,###")
        return "Rp ${decimalFormat.format(price)}"
    }
}
