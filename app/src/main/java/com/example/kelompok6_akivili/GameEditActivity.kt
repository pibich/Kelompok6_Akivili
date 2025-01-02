package com.example.kelompok6_akivili

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kelompok6_akivili.adapter.GameEditAdapter
import com.example.kelompok6_akivili.model.Game
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class GameEditActivity : AppCompatActivity() {

    private lateinit var gameIcon: ImageView
    private lateinit var inputGameName: EditText
    private lateinit var inputGameDescription: EditText
    private lateinit var buttonChangeIcon: MaterialButton
    private lateinit var buttonSaveGame: MaterialButton
    private lateinit var buttonAddNominal: MaterialButton
    private lateinit var storageRef: StorageReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageButton
    private lateinit var itemNominalContainer: LinearLayout
    private lateinit var inputNominalName: EditText
    private lateinit var inputNominalPrice: EditText
    private lateinit var recyclerViewGames: RecyclerView
    private lateinit var gameAdapter: GameEditAdapter
    private lateinit var buttonRefresh: Button

    private var gameId: String? = null
    private var gameImageUri: Uri? = null
    private val nominalList = mutableListOf<Pair<String, String>>() // For storing nominal name and price
    private var gameList = mutableListOf<Game>()
    private var isNominalAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_edit_admin)

        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("Games")

        gameIcon = findViewById(R.id.gameIcon)
        inputGameName = findViewById(R.id.inputGameName)
        inputGameDescription = findViewById(R.id.inputGameDescription)
        buttonChangeIcon = findViewById(R.id.buttonChangeIcon)
        buttonSaveGame = findViewById(R.id.buttonAddGame)
        buttonAddNominal = findViewById(R.id.buttonAddNominal)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        backButton = findViewById(R.id.backButton)
        itemNominalContainer = findViewById(R.id.itemNominalContainer)
        inputNominalName = findViewById(R.id.inputItemName)
        inputNominalPrice = findViewById(R.id.inputItemPrice)
        recyclerViewGames = findViewById(R.id.recyclerViewGames)
        buttonRefresh = findViewById(R.id.buttonRefresh)

        gameList = mutableListOf()

        // Set LayoutManager untuk RecyclerView
        recyclerViewGames.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL , false)

        // Set Adapter untuk RecyclerView
        gameAdapter = GameEditAdapter(this, gameList) { game ->
            // Handle game click
            val intent = Intent(this, EditGameActivity::class.java)
            intent.putExtra("gameId", game.id)  // Pass the gameId to the EditGameActivity
            startActivity(intent)
            Toast.makeText(this, "Game clicked: ${game.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerViewGames.adapter = gameAdapter

        // Fetch data from Firestore
        gameAdapter.fetchGamesData()

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        gameId = intent.getStringExtra("gameId") // Get game ID if editing an existing game

        if (gameId != null) {
            loadGameData(gameId!!)
            Log.e("GameEditActivity", "gameId is not null")
        }

        Log.e("GameEditActivity", "id: $gameId")

        buttonChangeIcon.setOnClickListener {
            // Allow admin to pick image
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        buttonSaveGame.setOnClickListener {
            // Validasi sebelum menyimpan
            if (isValidForm() && isNominalAdded) {
                saveGameData()
            } else {
                Toast.makeText(this, "Please fill in all fields and Add Nominal", Toast.LENGTH_SHORT).show()
            }
        }

        buttonRefresh.setOnClickListener {
            refreshGameList()
        }

        buttonAddNominal.setOnClickListener {
            val nominalName = inputNominalName.text.toString().trim()
            val nominalPrice = inputNominalPrice.text.toString().trim()

            addNominalInput(nominalName, nominalPrice)
            inputNominalName.text.clear()
            inputNominalPrice.text.clear()
            isNominalAdded = true

        }

        bottomNavigationView.selectedItemId = R.id.nav_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileAdminActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Add the TextWatcher to format the price input
        inputNominalPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val formattedText = formatInputPrice(it.toString())
                    inputNominalPrice.removeTextChangedListener(this) // Prevent recursion
                    inputNominalPrice.setText(formattedText)
                    inputNominalPrice.setSelection(formattedText.length)  // Set cursor at the end
                    inputNominalPrice.addTextChangedListener(this) // Add the listener back
                }
            }

            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Function to refresh the game list
    private fun refreshGameList() {
        // Fetch the data again from Firestore and update the RecyclerView
        gameAdapter.fetchGamesData()  // This will trigger the data fetching again
        Toast.makeText(this, "Refreshing Game List...", Toast.LENGTH_SHORT).show()
    }

    private fun isValidForm(): Boolean {
        // Cek jika gambar telah dipilih
        if (gameImageUri == null) {
            Toast.makeText(this, "Please select a game icon", Toast.LENGTH_SHORT).show()
            return false
        }

        // Cek apakah nama game dan deskripsi telah diisi
        val gameName = inputGameName.text.toString().trim()
        val gameDescription = inputGameDescription.text.toString().trim()
        if (gameName.isEmpty() || gameDescription.isEmpty()) {
            Toast.makeText(this, "Please fill in game name and description", Toast.LENGTH_SHORT).show()
            return false
        }

        // Cek apakah input nominal telah diisi
        if (!isNominalValid()) {
            Toast.makeText(this, "Please add at least one nominal value", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun isNominalValid(): Boolean {
        // Cek input yang ada di itemNominalContainer
        for (i in 0 until itemNominalContainer.childCount) {
            val nominalView = itemNominalContainer.getChildAt(i) as LinearLayout
            val inputItemName = nominalView.findViewById<EditText>(R.id.inputItemName).text.toString().trim()
            val inputItemPrice = nominalView.findViewById<EditText>(R.id.inputItemPrice).text.toString().trim()

            if (inputItemName.isEmpty() || inputItemPrice.isEmpty()) {
                return false
            }

            if (!isNominalAdded) { // Variable to check if the button was clicked
                Toast.makeText(this, "Please click 'Add Nominal' to add a nominal", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    // Load data game berdasarkan ID untuk editing
    private fun loadGameData(gameId: String) {
        // Menambahkan log untuk memeriksa saat loadGameData dipanggil
        Log.d("GameEditActivity", "Memulai pengambilan data game dengan ID: $gameId")

        firestore.collection("Games").document(gameId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val game = document.toObject(Game::class.java)
                    inputGameName.setText(game?.name)
                    inputGameDescription.setText(game?.description)

                    // Memuat gambar
                    game?.image?.let {
                        Glide.with(this).load(it).into(gameIcon)
                    }

                    // Menetapkan data nominal jika tersedia
                    val existingNominals = game?.nominalList ?: emptyList()
                    nominalList.clear()
                    nominalList.addAll(existingNominals)

                    // Menambahkan log untuk memeriksa jika data nominal berhasil dimuat
                    Log.d("GameEditActivity", "Data nominal berhasil dimuat: ${nominalList.size} item")

                    // Memastikan RecyclerView menerima data yang baru
                    gameAdapter.updateList(gameList)
                    Log.d("GameEditActivity", "RecyclerView diupdate dengan ${gameList.size} game.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("GameEditActivity", "Gagal mengambil dokumen: ${exception.message}")
                Toast.makeText(this, "Gagal mengambil dokumen: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk menyimpan data game ke Firestore
    private fun saveGameData() {
        val gameName = inputGameName.text.toString().trim()
        val gameDescription = inputGameDescription.text.toString().trim()

        if (gameName.isEmpty() || gameDescription.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data nominal yang terbaru dari input field (baik di activity maupun item nominal input)
        val formattedNominals = mutableListOf<Map<String, String>>()

        // Ambil data dari inputNominalName dan inputNominalPrice yang ada di layout activity_game_edit_admin
        val nominalName = inputNominalName.text.toString().trim()
        val nominalPrice = inputNominalPrice.text.toString().trim()

        // Jika data pada inputNominalName dan inputNominalPrice tidak kosong, tambahkan ke list nominal
        if (nominalName.isNotEmpty() && nominalPrice.isNotEmpty()) {
            formattedNominals.add(mapOf("nominal" to nominalName, "price" to formatPrice(nominalPrice)))
        }

        // Ambil data dari itemNominalContainer (inputItemNameNew dan inputItemPriceNew) yang sudah ditambahkan
        for (i in 0 until itemNominalContainer.childCount) {
            val nominalView = itemNominalContainer.getChildAt(i) as LinearLayout
            val inputItemName = nominalView.findViewById<EditText>(R.id.inputItemName).text.toString().trim()
            val inputItemPrice = nominalView.findViewById<EditText>(R.id.inputItemPrice).text.toString().trim()

            if (inputItemName.isNotEmpty() && inputItemPrice.isNotEmpty()) {
                formattedNominals.add(mapOf("nominal" to inputItemName, "price" to formatPrice(inputItemPrice)))
            }
        }

        val gameData = hashMapOf(
            "description" to gameDescription,
            "id" to generateRandomId(),
            "name" to gameName,
            "nominalList" to formattedNominals // Save formatted nominal and price
        )

        if (gameImageUri != null) {
            // Store image with game name
            val imageRef = storageRef.child("${gameName}.jpg")
            imageRef.putFile(gameImageUri!!).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    gameData["imageUrl"] = uri.toString()
                    saveGameToFirestore(gameName, gameData)
                }
            }
        } else {
            saveGameToFirestore(gameName, gameData)
        }
    }

    private fun formatPrice(price: String): String {
        // Remove any existing currency symbols or formatting
        val cleanedPrice = price.replace(Regex("[^\\d]"), "")
        val formattedPrice = cleanedPrice.toIntOrNull()?.let {
            val decimalFormat = java.text.DecimalFormat("#,###")
            "Rp ${decimalFormat.format(it)}"
        } ?: "Rp 0"
        return formattedPrice
    }

    private fun generateRandomId(): String {
        // Generate random UUID string to be used as a unique game ID
        return UUID.randomUUID().toString()
    }

    private fun saveGameToFirestore(gameName: String, gameData: HashMap<String, Any>) {
        // Simpan data dengan nama game sebagai ID dokumen
        firestore.collection("Games").document(gameName).set(gameData)
            .addOnSuccessListener {
                Toast.makeText(this, "Game saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save game", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNominalInput(nominalName: String, nominalPrice: String) {
        // Inflate new view for the nominal input
        val newNominalView = layoutInflater.inflate(R.layout.item_nominal_input, null)

        // Bind input elements
        val inputItemNameNew: EditText = newNominalView.findViewById(R.id.inputItemName)
        val inputItemPriceNew: EditText = newNominalView.findViewById(R.id.inputItemPrice)

        // Set values for new inputs
        inputItemNameNew.setText(nominalName)
        inputItemPriceNew.setText(nominalPrice)

        // Add the new view to the container, making sure it goes under the existing ones
        itemNominalContainer.addView(newNominalView)

        // Optionally add this new input to the list
        nominalList.add(Pair(nominalName, nominalPrice))
    }

    private fun formatInputPrice(input: String): String {
        // Remove non-digit characters first
        val cleanInput = input.replace(Regex("[^\\d]"), "")

        // Format it with thousand separator
        val formattedInput = if (cleanInput.isNotEmpty()) {
            val decimalFormat = java.text.DecimalFormat("#,###")
            "Rp ${decimalFormat.format(cleanInput.toLong())}"
        } else {
            ""
        }
        return formattedInput
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            gameImageUri = data?.data
            gameIcon.setImageURI(gameImageUri)
        }
    }
}
