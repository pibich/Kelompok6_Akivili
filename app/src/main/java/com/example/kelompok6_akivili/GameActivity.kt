package com.example.kelompok6_akivili

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import CartItem
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore

class GameActivity : AppCompatActivity() {

    companion object {
        val cartItems = mutableListOf<CartItem>()
    }

    private var selectedNominal: Pair<String, String>? = null
    private var selectedButton: Button? = null
    private val db = FirebaseFirestore.getInstance()  // Firestore instance
    private val auth = FirebaseAuth.getInstance()     // Auth instance
    private lateinit var gridNominal: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val gameImage = intent.getStringExtra("gameImage") ?: ""
        val gameName = intent.getStringExtra("gameName") ?: "Game Name"
        val gameDescription = intent.getStringExtra("gameDescription") ?: "No description available."
        val nominalList = intent.getSerializableExtra("nominalList") as? List<Pair<String, String>> ?: emptyList()
        val emailEditText: EditText = findViewById(R.id.inputEmail)  // Ambil input email

        val gameImageView: ImageView = findViewById(R.id.gameImage)
        val gameTitleTextView: TextView = findViewById(R.id.gameTitle)
        val gameDescriptionTextView: TextView = findViewById(R.id.gameDescription)
        gridNominal = findViewById(R.id.gridNominal)

        // Memuat gambar menggunakan Glide dari URL
        if (gameImage.isNotEmpty()) {
            Glide.with(this)
                .load(gameImage)  // Memuat gambar dari URL
                .into(gameImageView)
        } else {
            gameImageView.setImageResource(R.drawable.ic_launcher_foreground) // Atur gambar default jika kosong
        }

        gameTitleTextView.text = gameName
        gameDescriptionTextView.text = gameDescription

        populateNominalGrid(nominalList)

        val buyButton: Button = findViewById(R.id.buttonBuy)
        val addToCartButton: Button = findViewById(R.id.buttonAddToCart)

        buyButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            // Validasi email
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Masukkan email yang valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek apakah email sesuai dengan akun yang sedang login
            val currentUser = auth.currentUser
            if (currentUser == null || currentUser.email != email) {
                Toast.makeText(this, "Email harus sesuai dengan akun yang sedang digunakan!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (selectedNominal != null) {
                val price = selectedNominal!!.second.replace("Rp ", "").replace(".", "").toInt()

                // Kirim data menggunakan Intent tanpa menggunakan CartItem
                val intent = Intent(this, PurchaseMethodActivity::class.java).apply {
                    putExtra("gameName", gameName)
                    putExtra("nominal", selectedNominal!!.first)
                    putExtra("price", price)  // Harga item
                    putExtra("email", email)
                    putExtra("orderNumber", UUID.randomUUID().toString())
                    putExtra("isBuyNow", true)  // Menandakan pembelian langsung
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Pilih nominal terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }

        addToCartButton.setOnClickListener {
            handleAddToCart(gameImage, gameName)
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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
    }

    // Membuat tombol dinamis dari daftar nominal
    private fun populateNominalGrid(nominalList: List<Pair<String, String>>) {
        gridNominal.removeAllViews()

        for (nominal in nominalList) {
            val button = Button(this).apply {
                text = "${nominal.first}\n${nominal.second}"
                setBackgroundColor(ContextCompat.getColor(context, R.color.orange))
                setTextColor(Color.WHITE)

                setOnClickListener {
                    toggleNominalSelection(this, nominal)
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }
            button.layoutParams = params
            gridNominal.addView(button)
        }
    }

    // Fungsi untuk memilih/membatalkan tombol nominal
    private fun toggleNominalSelection(button: Button, nominal: Pair<String, String>) {
        if (selectedNominal == nominal) {
            // Jika tombol yang sama diklik lagi, batalkan pilihan
            selectedNominal = null
            selectedButton = null
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
            button.setTextColor(Color.WHITE)
            Toast.makeText(this, "Nominal dibatalkan.", Toast.LENGTH_SHORT).show()
        } else {
            // Reset tombol sebelumnya jika ada
            selectedButton?.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
            selectedButton?.setTextColor(Color.WHITE)

            // Pilih tombol baru
            selectedNominal = nominal
            selectedButton = button
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            button.setTextColor(Color.BLACK)

            Toast.makeText(this, "Nominal: ${nominal.first} dipilih.", Toast.LENGTH_SHORT).show()
        }
    }

    // Logika menambahkan ke keranjang
    private fun handleAddToCart(gameImage: String, gameName: String) {
        val emailEditText: EditText = findViewById(R.id.inputEmail)
        val email = emailEditText.text.toString()

        // Validasi email
        if (email.isEmpty() || !isValidEmail(email)) {
            Toast.makeText(this, "Masukkan email yang valid!", Toast.LENGTH_SHORT).show()
            return
        }

        selectedNominal?.let { nominal ->
            val price = nominal.second.replace("Rp ", "").replace(".", "").toInt()

            // Cek apakah item sudah ada di keranjang
            val existingItem = cartItems.find { it.name == gameName && it.price == price }

            if (existingItem != null) {
                existingItem.quantity += 1
            } else {
                // Membuat CartItem dengan nominalList yang dipilih
                cartItems.add(CartItem(gameImage, gameName, price, 1, nominalList = listOf(nominal)))
            }

            Toast.makeText(this, "Item ditambahkan ke keranjang.", Toast.LENGTH_SHORT).show()

            val formattedPrice = formatPrice(price)

            // Kirim data ke CartActivity
            val intent = Intent(this, CartActivity::class.java).apply {
                putExtra("userEmail", email)
                putExtra("gameName", gameName)
                putExtra("itemName", nominal.first)
                putExtra("totalPurchase", formattedPrice)
            }
            startActivity(intent)
        } ?: Toast.makeText(this, "Pilih nominal terlebih dahulu.", Toast.LENGTH_SHORT).show()
    }

    // Fungsi untuk memvalidasi email menggunakan regex
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Format price with "Rp" and thousand separators
    private fun formatPrice(price: Int): String {
        val decimalFormat = java.text.DecimalFormat("#,###")
        return "Rp ${decimalFormat.format(price)}"
    }
}
