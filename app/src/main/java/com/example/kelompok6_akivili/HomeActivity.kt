package com.example.kelompok6_akivili

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.kelompok6_akivili.adapter.GameAdapter
import com.example.kelompok6_akivili.adapter.ImageCarouselAdapter
import com.example.kelompok6_akivili.model.Game
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private lateinit var gameList: ArrayList<Game>

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var currentPosition = 0
    private val delay: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewPager = findViewById(R.id.viewPager)
        val images = listOf(
            R.drawable.car3,
            R.drawable.car2,
            R.drawable.car1
        )
        val imageCarouselAdapter = ImageCarouselAdapter(this, images)
        viewPager.adapter = imageCarouselAdapter

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (currentPosition == imageCarouselAdapter.count) {
                    currentPosition = 0
                }
                viewPager.setCurrentItem(currentPosition++, true)
                handler.postDelayed(this, delay)
            }
        }
        handler.postDelayed(runnable, delay)

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        clearButton = findViewById(R.id.clearButton)

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

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        gameList = arrayListOf(
            Game(R.drawable.hsr1, "Honkai: Star Rail"),
            Game(R.drawable.gi1, "Genshin Impact"),
            Game(R.drawable.hi31, "Honkai Impact 3rd"),
            Game(R.drawable.mlbb1, "Mobile Legends: Bang Bang"),
            Game(R.drawable.wuwa1, "Wuthering Waves"),
            Game(R.drawable.ff1, "Free Fire")
        )

        gameAdapter = GameAdapter(this, gameList)
        recyclerView.adapter = gameAdapter

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
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

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, delay)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_home
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

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }
}
