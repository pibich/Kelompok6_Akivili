package com.example.kelompok6_akivili

import CartItem
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kelompok6_akivili.adapter.CartAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class CartActivity : AppCompatActivity() {

    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var buyButton: MaterialButton
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var selectAllCheckBox: CheckBox

    private val cartItems: MutableList<CartItem> = GameActivity.cartItems ?: mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Bind views
        totalTextView = findViewById(R.id.totalPurchaseTextView)
        recyclerView = findViewById(R.id.recyclerViewCart)
        buyButton = findViewById(R.id.buttonBuyCart)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(this, cartItems) {
            updateTotal()
        }
        recyclerView.adapter = cartAdapter

        // Setup Select All CheckBox
        selectAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
            cartItems.forEach { it.isSelected = isChecked }
            cartAdapter.notifyDataSetChanged()
            updateTotal()
        }

        // Setup Buy Button
        buyButton.setOnClickListener {
            handlePurchase()
        }

        // Setup Bottom Navigation
        bottomNavigationView.selectedItemId = R.id.nav_cart
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_cart -> true
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

        // Update total price
        updateTotal()
    }

    // Update total harga saat checkbox item dipilih
    private fun updateTotal() {
        val total = cartItems.filter { it.isSelected }.sumOf { it.price * it.quantity }
        totalTextView.text = formatPrice(total)

        // Periksa apakah keranjang kosong setelah update
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong. Tambahkan item baru.", Toast.LENGTH_SHORT).show()
            selectAllCheckBox.isChecked = false
        }
    }

    // Handle ketika tombol beli diklik
    private fun handlePurchase() {
        val selectedItems = cartItems.filter { it.isSelected }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong. Tidak ada item untuk dibeli.", Toast.LENGTH_SHORT).show()
            return
        }

        // Kelompokkan item berdasarkan nama game dan nominal
        val groupedItems = selectedItems.groupBy { it.name to it.nominalList }

        val gameNames = mutableListOf<String>()
        val itemNames = mutableListOf<String>()
        val totalPrices = mutableListOf<Int>()
        val quantities = mutableListOf<Int>()

        // Gabungkan nama game, item, harga, dan jumlah item yang dibeli
        groupedItems.forEach { (key, items) ->
            val gameName = key.first // Nama game
            val nominalList = key.second // List dari Pair<String, String> (nominal)

            // Mengakses harga dan jumlah item
            val totalPrice = items.sumOf { it.price * it.quantity }
            val quantity = items.sumOf { it.quantity }

            gameNames.add(gameName)

            // Untuk setiap nominal di dalam nominalList, format sebagai "nominal.first - nominal.second"
            nominalList.forEach { nominal ->
                val itemName = "${nominal.first} -> ${nominal.second}"  // Format nominal
                itemNames.add(itemName)
            }

            totalPrices.add(totalPrice)
            quantities.add(quantity)
        }

        val formattedGameNames = gameNames.joinToString(", ")
        val formattedItemNames = itemNames.joinToString(", ")

        val totalSelectedPrice = totalPrices.sum()

        // Kirim data yang sudah diformat ke PurchaseMethodActivity
        val intent = Intent(this, PurchaseMethodActivity::class.java).apply {
            putExtra("userEmail", intent.getStringExtra("userEmail"))
            putExtra("gameNames", formattedGameNames)
            putExtra("itemNames", formattedItemNames)
            putExtra("totalPrices", totalPrices.toIntArray())  // Kirim harga total per item
            putExtra("quantities", quantities.toIntArray())    // Kirim jumlah item yang dibeli
        }

        // Konfirmasi sebelum lanjut ke halaman pembayaran
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembelian")
            .setMessage("Apakah Anda yakin ingin membeli semua item yang dipilih?")
            .setPositiveButton("Lanjutkan") { _, _ -> startActivity(intent) }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Ketika kembali ke CartActivity, item yang dibeli dihapus
    override fun onResume() {
        super.onResume()
        cartAdapter.notifyDataSetChanged() // Pastikan RecyclerView terupdate
        updateTotal() // Perbarui total harga
    }

    // Format price with "Rp" and thousand separators
    private fun formatPrice(price: Int): String {
        val decimalFormat = java.text.DecimalFormat("#,###")
        return "Rp ${decimalFormat.format(price)}"
    }
}
