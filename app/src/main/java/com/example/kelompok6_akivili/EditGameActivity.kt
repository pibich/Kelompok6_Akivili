package com.example.kelompok6_akivili

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kelompok6_akivili.model.Game
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID


// Modifikasi pada EditGameActivity untuk menerima data dari Intent
class EditGameActivity : AppCompatActivity() {

    private lateinit var gameIcon: ImageView
    private lateinit var inputGameName: EditText
    private lateinit var inputGameDescription: EditText
    private lateinit var buttonAddNominal: MaterialButton
    private lateinit var buttonChangeIcon: Button
    private lateinit var buttonSaveGame: Button
    private lateinit var itemNominalContainer: LinearLayout
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageButton
    private var gameId: String? = null
    private var gameImageUri: Uri? = null

    private val nominalList = mutableListOf<Pair<String, String>>() // For storing nominal name and price

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_game)

        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("Games")

        gameIcon = findViewById(R.id.gameIcon)
        inputGameName = findViewById(R.id.inputGameName)
        inputGameDescription = findViewById(R.id.inputGameDescription)
        buttonChangeIcon = findViewById(R.id.buttonChangeIcon)
        buttonSaveGame = findViewById(R.id.buttonSaveGame)
        itemNominalContainer = findViewById(R.id.itemNominalContainer)
        buttonAddNominal = findViewById(R.id.buttonAddNominal)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        backButton = findViewById(R.id.backButton)

        // Menerima data dari Intent
        gameId = intent.getStringExtra("gameId") // Dapatkan ID game
        Log.d("EditGameActivity", "gameId sewaktu ambil: $gameId")
        val gameName = intent.getStringExtra("gameName") ?: ""
        val gameDescription = intent.getStringExtra("gameDescription") ?: ""
        val gameImage = intent.getStringExtra("gameImage") ?: ""
        val nominalListFromIntent = intent.getSerializableExtra("nominalList") as? List<Pair<String, String>> ?: emptyList()

        // Set data ke UI
        inputGameName.setText(gameName)
        inputGameDescription.setText(gameDescription)

        // Load image from URL
        Glide.with(this)
            .load(gameImage)
            .into(gameIcon)

        // Mengisi list nominal jika ada
        nominalList.clear()
        nominalList.addAll(nominalListFromIntent)
        for (nominal in nominalList) {
            addNominalView(nominal.first, nominal.second)
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Button click listener untuk ganti gambar
        buttonChangeIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // Button click listener untuk simpan data
        buttonSaveGame.setOnClickListener {
            saveEditedGameData()
        }

        // Button untuk menambahkan nominal baru
        buttonAddNominal.setOnClickListener {
            val nominalName = ""
            val nominalPrice = ""
            addNominalView(nominalName, nominalPrice)
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
    }

    // Fungsi untuk menambahkan tampilan nominal baru
    private fun addNominalView(nominalName: String, nominalPrice: String) {
        val newNominalView = layoutInflater.inflate(R.layout.item_nominal_input, null)
        val inputItemNameNew: EditText = newNominalView.findViewById(R.id.inputItemName)
        val inputItemPriceNew: EditText = newNominalView.findViewById(R.id.inputItemPrice)

        inputItemNameNew.setText(nominalName)
        inputItemPriceNew.setText(nominalPrice)

        itemNominalContainer.addView(newNominalView)
    }

    // Fungsi untuk menyimpan data game yang telah diedit
    private fun saveEditedGameData() {
        val gameName = inputGameName.text.toString().trim()
        val gameDescription = inputGameDescription.text.toString().trim()

        if (gameName.isEmpty() || gameDescription.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data nominal yang terbaru dari input field
        val formattedNominals = mutableListOf<Map<String, String>>()
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
            "name" to gameName,
            "nominalList" to formattedNominals
        )

        // Add ID to the map, but only if gameId is non-null
        gameId?.let {
            gameData["id"] = it
            Log.d("EditGameActivity", "gameId tidak null: $gameId")
        }

        // If the game name is changed, we need to update the image name in Firebase Storage
        if (gameImageUri != null || gameName != inputGameName.text.toString().trim()) {
            // Get old image name from the gameId (If available)
            val oldImageName = gameName // or get the previous name from your database if it's not passed
            val newImageName = "${gameName}.jpg" // The new name for the image

            if (oldImageName != newImageName) {
                // Delete the old image from Firebase Storage if the name has changed
                val oldImageRef = storageRef.child(oldImageName) // Use the already declared storageRef
                oldImageRef.delete().addOnSuccessListener {
                    Log.d("EditGameActivity", "Old image deleted from Storage")
                }.addOnFailureListener { exception ->
                    Log.e("EditGameActivity", "Failed to delete old image: ${exception.message}")
                }
            }

            // Upload the new image and update the image URL in Firestore
            val newImageRef = storageRef.child(newImageName) // Use the already declared storageRef
            newImageRef.putFile(gameImageUri!!).addOnSuccessListener {
                newImageRef.downloadUrl.addOnSuccessListener { uri ->
                    gameData["imageUrl"] = uri.toString()  // Update game data with the new image URL
                    updateGameInFirestore(gameData)
                }
            }
        } else {
            updateGameInFirestore(gameData)  // Update without new image if not selected
        }

        Log.d("EditGameActivity", "gameId setelah di-save: $gameId")
    }

    private fun updateGameInFirestore(gameData: HashMap<String, Any>) {
        if (gameId != null) {
            // Cari dokumen di collection "Games" yang memiliki field "id" yang sama dengan gameId
            firestore.collection("Games")
                .whereEqualTo("id", gameId)  // Gunakan field "id" untuk mencari dokumen yang tepat
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Pastikan dokumen ditemukan
                    if (!querySnapshot.isEmpty) {
                        // Ambil document ID yang ditemukan
                        val documentId = querySnapshot.documents[0].id  // Ambil document ID dari hasil query

                        // Lakukan update pada dokumen yang ditemukan menggunakan document ID
                        firestore.collection("Games").document(documentId)
                            .update(gameData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Game updated successfully", Toast.LENGTH_SHORT).show()
                                Log.d("EditGameActivity", "Game updated with ID: $documentId")
                                finish()  // Close the activity after success
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed to update game: ${exception.message}", Toast.LENGTH_SHORT).show()
                                Log.e("EditGameActivity", "Failed to update game: ${exception.message}")
                            }
                    } else {
                        // Jika dokumen tidak ditemukan dengan id yang diberikan
                        Toast.makeText(this, "No game found with this ID", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to find game: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditGameActivity", "Error getting document: ${exception.message}")
                }
        } else {
            // Jika gameId null, beri tahu pengguna bahwa ID tidak ditemukan
            Toast.makeText(this, "Game ID is missing, cannot update.", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk memformat harga dengan format mata uang
    private fun formatPrice(price: String): String {
        val cleanedPrice = price.replace(Regex("[^\\d]"), "")
        return if (cleanedPrice.isNotEmpty()) {
            val formattedPrice = cleanedPrice.toIntOrNull()?.let {
                java.text.DecimalFormat("#,###").format(it)
            } ?: ""
            "Rp $formattedPrice"
        } else {
            "Rp 0"
        }
    }

    // Fungsi untuk menangani hasil pemilihan gambar
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            gameImageUri = data?.data
            gameIcon.setImageURI(gameImageUri)
        }
    }
}
