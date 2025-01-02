package com.example.kelompok6_akivili

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.kelompok6_akivili.adapter.GameAdapter
import com.example.kelompok6_akivili.adapter.ImageCarouselAdapter
import com.example.kelompok6_akivili.model.Game
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class HomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private lateinit var gameList: ArrayList<Game>
    private lateinit var customerServiceIcon: ImageView
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var currentPosition = 0
    private val delay: Long = 3000
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var dotsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })

        customerServiceIcon = findViewById(R.id.customerServiceIcon)
        customerServiceIcon.setOnClickListener {
            val intent = Intent(this, CustomerServiceActivity::class.java)
            startActivity(intent)
        }

        viewPager = findViewById(R.id.viewPager)
        dotsLayout = findViewById(R.id.dotsLayout)
        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        clearButton = findViewById(R.id.clearButton)

        recyclerView.layoutManager = GridLayoutManager(this, 3)

        gameList = arrayListOf()
        gameAdapter = GameAdapter(this, gameList) { game -> // Corrected to use this (context) and gameList
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("gameImage", game.image)
            intent.putExtra("gameName", game.name)
            intent.putExtra("gameDescription", game.description)
            intent.putExtra("nominalList", ArrayList(game.nominalList))
            startActivity(intent)
        }
        recyclerView.adapter = gameAdapter

        loadCarouselImages()
        loadGameList()

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (currentPosition == viewPager.adapter?.count) {
                    currentPosition = 0
                }
                viewPager.setCurrentItem(currentPosition++, true)
                handler.postDelayed(this, delay)
            }
        }
        handler.postDelayed(runnable, delay)

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString()
            filterGames(searchText)
        }

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            gameAdapter.updateList(gameList)
        }

        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val searchText = searchEditText.text.toString()
                filterGames(searchText)
                true
            } else {
                false
            }
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
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

    private fun loadCarouselImages() {
        val storageRef = storage.reference.child("ViewPager")

        storageRef.listAll().addOnSuccessListener { result ->
            val imageUrls = mutableListOf<String>()
            val items = result.items
            var counter = 0
            val totalItems = items.size  // Get the size of the images

            for (item in items) {
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageUrls.add(uri.toString())
                    counter++

                    // Update dots after all items are fetched
                    if (counter == totalItems) {
                        updateCarousel(imageUrls, totalItems)
                    }
                }.addOnFailureListener {
                    imageUrls.add("")
                    counter++

                    if (counter == totalItems) {
                        updateCarousel(imageUrls, totalItems)
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memuat gambar carousel", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateCarousel(imageUrls: List<String>, totalItems: Int) {
        val adapter = ImageCarouselAdapter(this, imageUrls)
        viewPager.adapter = adapter

        // Create dots based on the number of items
        dotsLayout.removeAllViews()  // Clear existing dots
        for (i in 0 until totalItems) {
            val dot = ImageView(this)
            dot.setImageResource(R.drawable.dot_inactive) // Default inactive state
            val params = LinearLayout.LayoutParams(16, 16)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            dotsLayout.addView(dot)
        }

        // Initially set the first dot to active
        if (dotsLayout.childCount > 0) {
            val firstDot = dotsLayout.getChildAt(0) as ImageView
            firstDot.setImageResource(R.drawable.dot_active) // Set the first dot active
        }

        // Set the ViewPager to the first page
        viewPager.setCurrentItem(0, false)

        // Synchronize the dots with the ViewPager's position
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                // Update dots to reflect the current position
                for (i in 0 until dotsLayout.childCount) {
                    val dot = dotsLayout.getChildAt(i) as ImageView
                    if (i == position) {
                        dot.setImageResource(R.drawable.dot_active)
                    } else {
                        dot.setImageResource(R.drawable.dot_inactive)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun loadGameList() {
        db.collection("Games").get().addOnSuccessListener { documents ->
            gameList.clear()
            var counter = 0
            val totalDocuments = documents.size()

            for (document in documents) {
                val name = document.getString("name") ?: ""
                val description = document.getString("description") ?: ""
                val nominalList = document.get("nominalList") as? List<Map<String, String>> ?: emptyList()

                val mappedNominals = nominalList.map {
                    Pair(it["nominal"] ?: "", it["price"] ?: "")
                }

                val storageRef = storage.getReference("Games/$name.jpg")

                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    gameList.add(Game(imageUrl, name, description, mappedNominals))
                    counter++
                    if (counter == totalDocuments) {
                        gameAdapter.notifyDataSetChanged()
                    }
                }.addOnFailureListener {
                    gameList.add(Game("", name, description, mappedNominals))
                    counter++
                    if (counter == totalDocuments) {
                        gameAdapter.notifyDataSetChanged()
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Gagal memuat daftar game: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun filterGames(searchText: String) {
        val filteredList = gameList.filter {
            it.name.contains(searchText, ignoreCase = true)
        }
        if (filteredList.isEmpty()) {
            showNoGamesFoundDialog()
        } else {
            gameAdapter.updateList(filteredList)
        }
    }

    private fun showNoGamesFoundDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search Result")
        builder.setMessage("No games found.")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Keluar")
        builder.setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
        builder.setPositiveButton("Ya") { _, _ -> finish() }
        builder.setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}
