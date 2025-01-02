package com.example.kelompok6_akivili

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.SimpleDateFormat
import PurchaseHistoryManager
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kelompok6_akivili.GameActivity.Companion.cartItems
import java.util.*

class PurchaseActivity : AppCompatActivity() {

    private lateinit var qrCodeImage: ImageView
    private lateinit var paymentStatus: TextView
    private lateinit var paymentTimer: TextView
    private lateinit var buttonContainer: LinearLayout
    private lateinit var viewOrderButton: Button
    private lateinit var shopAgainButton: Button

    private var countdownTimer: CountDownTimer? = null
    private var isPaid = false
    private val countdownTimeMillis: Long = 1 * 60 * 1000 // Timer 1 menit
    private val db = FirebaseFirestore.getInstance()  // Firestore instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)

        qrCodeImage = findViewById(R.id.qrCodeImage)
        paymentStatus = findViewById(R.id.paymentStatus)
        paymentTimer = findViewById(R.id.paymentTimer)
        buttonContainer = findViewById(R.id.buttonContainer)
        viewOrderButton = findViewById(R.id.viewOrderButton)
        shopAgainButton = findViewById(R.id.shopAgainButton)

        buttonContainer.visibility = View.GONE

        // Ambil data dari Intent
        val paymentMethod = intent.getStringExtra("paymentMethod") ?: "Tidak Diketahui"
        val totalPurchase = intent.getIntExtra("totalPurchase", 0)
        val orderNumber = intent.getStringExtra("orderNumber") ?: "INV${System.currentTimeMillis()}"
        val userEmail = intent.getStringExtra("userEmail") ?: "Email tidak tersedia"
        val isBuyNow = intent.getBooleanExtra("isBuyNow", false) // Flag untuk menentukan beli langsung atau keranjang

        // Ambil nama game dan item yang dipilih
        val gameNames = intent.getStringExtra("gameNames") ?: "Game Tidak Diketahui"
        val itemNames = intent.getStringExtra("itemNames") ?: "Item Tidak Diketahui"
        val totalPrices = intent.getIntArrayExtra("totalPrices") ?: IntArray(0)
        val quantities = intent.getIntArrayExtra("quantities") ?: IntArray(0)

        val email = intent.getStringExtra("email") ?: ""
        val gameName = intent.getStringExtra("gameName") ?: "Tidak diketahui"
        val nominal = intent.getStringExtra("nominal") ?: "Tidak diketahui"
        val price = intent.getIntExtra("price", 0)

        // Menampilkan email
        val emailSummaryTextView: TextView = findViewById(R.id.emailSummaryTextView)
        emailSummaryTextView.text = "Email: $userEmail"

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Buat QR Code
        generateQRCode(orderNumber, qrCodeImage)

        // Jika dari keranjang atau beli langsung, tampilkan detail yang berbeda
        if (isBuyNow) {
            // Display data for Buy Now scenario
            setupPurchaseDetailsForBuyNow(paymentMethod, totalPurchase, orderNumber, email, gameName, nominal, price)
        } else {
            // Display data for Cart scenario
            setupPurchaseDetails(paymentMethod, totalPurchase, orderNumber, itemNames, gameNames, totalPrices, quantities, isBuyNow)
        }

        startCountdownTimer()

        qrCodeImage.setOnClickListener {
            simulatePayment(paymentMethod, totalPurchase, orderNumber, itemNames, gameNames, nominal, price, isBuyNow)
        }

        viewOrderButton.setOnClickListener {
            startActivity(Intent(this, PurchaseHistoryActivity::class.java))
        }

        shopAgainButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

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
    }

    // Setup detail pembelian untuk Buy Now (langsung beli)
    private fun setupPurchaseDetailsForBuyNow(
        paymentMethod: String,
        totalPurchase: Int,
        orderNumber: String,
        email: String,
        gameName: String,
        nominal: String,
        price: Int
    ) {
        val orderDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val orderStatus = "Status Pesanan: Menunggu Pembayaran"

        // Menampilkan nominal dan harga untuk item
        val itemDetails = "$nominal -> ${formatPrice(price)}"

        // Menampilkan detail pembelian di UI
        findViewById<TextView>(R.id.purchaseDate).text = "Tanggal Pembelian: $orderDate"
        findViewById<TextView>(R.id.orderNumber).text = "Nomor Pesanan: $orderNumber"
        findViewById<TextView>(R.id.paymentMethod).text = "Metode Pembayaran: $paymentMethod"
        findViewById<TextView>(R.id.totalAmount).text = "Total: Rp ${formatPrice(price)}"
        findViewById<TextView>(R.id.itemSummaryTextView).text = "Item: $itemDetails"
        findViewById<TextView>(R.id.gameSummaryTextView).text = "Game: $gameName"
        findViewById<TextView>(R.id.orderStatus).text = orderStatus
    }

    // Setup detail pembelian untuk Cart scenario
    private fun setupPurchaseDetails(
        paymentMethod: String,
        totalPurchase: Int,
        orderNumber: String,
        itemNames: String,
        gameNames: String,
        totalPrices: IntArray,
        quantities: IntArray,
        isBuyNow: Boolean
    ) {
        val orderDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val orderStatus = if (isBuyNow) "Status Pesanan: Menunggu Pembayaran" else "Status Pesanan: Diproses dari Keranjang"

        // Menampilkan item yang dibeli beserta kuantitas dan harga total
        val itemDetails = itemNames.split(",")
            .zip(quantities.toList())
            .zip(totalPrices.toList())
            .joinToString(", ") {
                "${it.first.first} x ${it.first.second} = Rp ${formatPrice(it.second)}"
            }

        // Menampilkan detail pembelian di UI
        findViewById<TextView>(R.id.purchaseDate).text = "Tanggal Pembelian: $orderDate"
        findViewById<TextView>(R.id.orderNumber).text = "Nomor Pesanan: $orderNumber"
        findViewById<TextView>(R.id.paymentMethod).text = "Metode Pembayaran: $paymentMethod"
        findViewById<TextView>(R.id.totalAmount).text = "Total: Rp ${formatPrice(totalPurchase)}"
        findViewById<TextView>(R.id.itemSummaryTextView).text = "Item: $itemDetails"
        findViewById<TextView>(R.id.gameSummaryTextView).text = "Game: $gameNames"
        findViewById<TextView>(R.id.orderStatus).text = orderStatus
    }

    // Timer untuk pembayaran
    private fun startCountdownTimer() {
        countdownTimer = object : CountDownTimer(countdownTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val seconds = (millisUntilFinished / 1000) % 60
                paymentTimer.text = String.format("%02d Menit : %02d Detik", minutes, seconds)
            }

            override fun onFinish() {
                paymentStatus.text = "Pesanan Expired"
                paymentTimer.text = "Waktu Pembayaran Habis"
                findViewById<TextView>(R.id.orderStatus).apply {
                    text = "Pembayaran Gagal"
                    setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                }

                // Nonaktifkan klik QR Code saat waktu habis
                disableQRCode()
            }
        }.start()
    }

    // Generate QR Code
    private fun generateQRCode(data: String, imageView: ImageView) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Simulasi pembayaran
    private fun simulatePayment(
        paymentMethod: String,
        totalPurchase: Int,
        orderNumber: String,
        itemNames: String,
        gameNames: String,
        nominal: String,
        price: Int,
        isBuyNow: Boolean // Flag untuk menentukan apakah dari Buy Now atau Cart
    ) {
        isPaid = true

        if (isPaid) {
            paymentStatus.text = "SUDAH BAYAR"
            paymentStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            findViewById<TextView>(R.id.orderStatus).apply {
                text = "Status Pesanan: Sedang Diproses"
                setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            }
            val timerInstruction: TextView = findViewById(R.id.timerInstruction)
            timerInstruction.visibility = View.GONE

            // Geser QR code ke atas agar button terlihat
            val qrCodeImage: ImageView = findViewById(R.id.qrCodeImage)
            qrCodeImage.setPadding(0, 0, 0, 0)
            val itemDetails = "$nominal -> Rp ${formatPrice(price)}"
            val quantities = intent.getIntArrayExtra("quantities") ?: IntArray(0)
            val totalPrices = intent.getIntArrayExtra("totalPrices") ?: IntArray(0)
            val itemNames = itemNames.split(",")
                .zip(quantities.toList())
                .zip(totalPrices.toList())
                .joinToString(", ") {
                    "${it.first.first} x ${it.first.second} = Rp ${formatPrice(it.second)}"
                }

            val gameName = intent.getStringExtra("gameName") ?: "Game Tidak Diketahui"

            paymentTimer.text = "Pembayaran Selesai"
            countdownTimer?.cancel()
            buttonContainer.visibility = View.VISIBLE

            // Nonaktifkan klik QR Code setelah pembayaran berhasil
            disableQRCode()

            // Tentukan nilai itemName, gameName, dan totalPurchase berdasarkan kondisi
            val itemName = if (isBuyNow) {
                itemDetails // Jika dari Buy Now, gunakan nilai nominal
            } else {
                itemNames // Jika dari Cart, gunakan itemNames
            }

            val gameNameFinal = if (isBuyNow) {
                gameName // Jika dari Buy Now, gunakan gameName
            } else {
                gameNames
            }

            val totalPurchaseFinal = if (isBuyNow) {
                price // Jika dari Buy Now, gunakan price
            } else {
                totalPurchase // Jika dari Cart, gunakan totalPurchase
            }

            // Simpan transaksi ke dalam histori dan Firebase
            val historyManager = PurchaseHistoryManager(this)
            val orderDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val userEmail = intent.getStringExtra("userEmail") ?: "Tidak Ada Email"

            val purchase = PurchaseHistory(
                date = orderDate,
                orderNumber = orderNumber,
                paymentMethod = paymentMethod,
                totalAmount = "Rp ${formatPrice(totalPurchaseFinal)}", // Menggunakan format harga
                status = "Sedang Diproses",
                email = userEmail,
                gameName = gameNameFinal, // Menggunakan gameNameFinal
                itemName = itemName // Menggunakan itemName yang sudah disesuaikan
            )

            // Simpan data ke SharedPreferences
            historyManager.savePurchase(purchase)

            // Simpan ke Firestore dengan penamaan yang konsisten (tidak terpisah)
            saveToFirestore(paymentMethod, totalPurchaseFinal, orderNumber, itemName, gameNameFinal, userEmail)

            // Menghapus item yang telah dibeli dari keranjang
            removePurchasedItemsFromCart()
        }
    }

    // Fungsi untuk menghapus item yang sudah dibeli dari keranjang
    private fun removePurchasedItemsFromCart() {
        // Dapatkan item yang dipilih (yang dicentang)
        val selectedItems = cartItems.filter { it.isSelected }

        // Jika ada item yang dipilih, hapus dari keranjang
        if (selectedItems.isNotEmpty()) {
            // Menghapus item yang dipilih dari cartItems
            cartItems.removeAll { it.isSelected }

            // Menampilkan pesan bahwa item telah dihapus dari keranjang
            Toast.makeText(this, "Item yang dibeli telah dihapus dari keranjang.", Toast.LENGTH_SHORT).show()
        } else {
            // Jika tidak ada item yang dipilih, tampilkan pesan
            Toast.makeText(this, "Tidak ada item yang dipilih untuk dihapus.", Toast.LENGTH_SHORT).show()
        }
    }

    // Simpan ke Firestore (Firebase Database)
    private fun saveToFirestore(
        paymentMethod: String,
        totalPurchase: Int,
        orderNumber: String,
        itemNames: String,
        gameNames: String,
        userEmail: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // Konversi timestamp ke format human-readable
        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        val purchaseHistoryData = hashMapOf(
            "userId" to userId,
            "userEmail" to userEmail,
            "gameName" to gameNames,
            "itemName" to itemNames,
            "totalAmount" to "Rp ${formatPrice(totalPurchase)}", // Menggunakan format harga
            "paymentMethod" to paymentMethod,
            "status" to "Sedang Diproses",
            "timestamp" to System.currentTimeMillis(),
            "orderDate" to formattedDate
        )

        db.collection("Purchase History")
            .document(orderNumber)  // Gunakan orderNumber sebagai ID dokumen
            .set(purchaseHistoryData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Pesanan berhasil disimpan di Firebase!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan ke Firebase: $e", Toast.LENGTH_LONG).show()
            }
    }

    // Fungsi untuk menonaktifkan QR Code
    private fun disableQRCode() {
        qrCodeImage.isEnabled = false  // Nonaktifkan klik
        qrCodeImage.alpha = 0.5f       // Redupkan tampilan QR Code
    }

    // Format price with "Rp" and thousand separators
    private fun formatPrice(price: Int): String {
        val decimalFormat = java.text.DecimalFormat("#,###")
        return decimalFormat.format(price)
    }
}
