package com.example.kelompok6_akivili

import PurchaseHistoryManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.widget.Toast
import android.net.Uri
import android.widget.ImageButton

class ProfileActivity : AppCompatActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var profileName: TextView
    private lateinit var changeEmailButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button
    private lateinit var purchaseHistoryRecyclerView: RecyclerView
    private lateinit var purchaseHistoryAdapter: PurchaseHistoryAdapter
    private lateinit var purchaseHistoryList: MutableList<PurchaseHistory>

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var purchaseHistoryManager: PurchaseHistoryManager

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePicture = findViewById(R.id.profilePicture)
        profileName = findViewById(R.id.profileName)
        changeEmailButton = findViewById(R.id.changeEmailButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        logoutButton = findViewById(R.id.logoutButton)
        purchaseHistoryRecyclerView = findViewById(R.id.purchaseHistoryRecyclerView)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadProfilePicture(it) }
        }

        purchaseHistoryManager = PurchaseHistoryManager(this)

        // Load histori pembelian dari SharedPreferences
        loadPurchaseHistory()

        loadUserProfile()

        profilePicture.setOnClickListener {
            showChangeProfilePictureDialog()
        }

        changeEmailButton.setOnClickListener {
            val intent = Intent(this, ChangeEmailActivity::class.java)
            startActivity(intent)
        }

        changePasswordButton.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val viewMoreButton: Button = findViewById(R.id.viewMoreButton)
        viewMoreButton.setOnClickListener {
            startActivity(Intent(this, PurchaseHistoryActivity::class.java))
        }

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
                R.id.nav_profile -> true
                else -> false
            }
        }

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // Load histori pembelian dari SharedPreferences
    private fun loadPurchaseHistory() {
        val allPurchases = purchaseHistoryManager.getAllPurchases()

        // Tampilkan hanya 2 transaksi terbaru
        purchaseHistoryList = allPurchases.take(2).toMutableList()

        // Setup RecyclerView
        purchaseHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        purchaseHistoryAdapter = PurchaseHistoryAdapter(this, purchaseHistoryList)
        purchaseHistoryRecyclerView.adapter = purchaseHistoryAdapter
    }

    // Load profil pengguna dari Firebase Firestore
    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        userId?.let {
            firestore.collection("users").document(it).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    profileName.text = "Hi, ${name ?: "Pengguna"}!"

                    val profilePicUrl = document.getString("profilePicture")
                    if (profilePicUrl != null) {
                        Glide.with(this)
                            .load(profilePicUrl)
                            .into(profilePicture)
                    } else {
                        profilePicture.setImageResource(R.drawable.baseline_account_circle_24)
                    }
                }
            }
        }
    }

    private fun showChangeProfilePictureDialog() {
        val options = arrayOf("Ganti Foto Profil", "Hapus Foto Profil", "Batal")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Opsi")
        builder.setItems(options) { dialog, which ->
            when (options[which]) {
                "Ganti Foto Profil" -> {
                    pickImageLauncher.launch("image/*")
                }
                "Hapus Foto Profil" -> {
                    deleteProfilePicture()
                }
                "Batal" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun uploadProfilePicture(uri: Uri) {
        profilePicture.setImageURI(uri)

        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/${auth.currentUser?.uid}")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val userId = auth.currentUser?.uid
                firestore.collection("users").document(userId!!)
                    .update("profilePicture", downloadUri.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            loadUserProfile()
                        } else {
                            Toast.makeText(this, "Gagal memperbarui gambar profil", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun deleteProfilePicture() {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/${auth.currentUser?.uid}")
        storageRef.delete().addOnSuccessListener {
            firestore.collection("users").document(auth.currentUser?.uid!!)
                .update("profilePicture", null)
            profilePicture.setImageResource(R.drawable.baseline_account_circle_24)
        }
    }
}
