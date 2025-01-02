package com.example.kelompok6_akivili

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kelompok6_akivili.adapter.ImageAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class ViewPagerManagementActivity : AppCompatActivity() {

    // Existing fields
    private lateinit var recyclerView: RecyclerView
    private lateinit var addImageButton: MaterialButton
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private val imageList = mutableListOf<String>() // Mutable list to hold image URLs
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager_admin)

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference.child("ViewPager")

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewViewPager)
        addImageButton = findViewById(R.id.addImageButton)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Initialize RecyclerView and Adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        imageAdapter = ImageAdapter(imageList, this) { imageUrl ->
            removeImage(imageUrl) // Remove image on click
        }
        recyclerView.adapter = imageAdapter

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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

        // Load images from Firebase Storage
        loadImagesFromStorage()

        // Launch image picker when add image button is clicked
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadImageToFirebase(it) }
        }

        addImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun loadImagesFromStorage() {
        storageRef.listAll().addOnSuccessListener { result ->
            val imageUrls = mutableListOf<String>()
            val items = result.items
            items.forEach { item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageUrls.add(uri.toString())
                    imageAdapter.updateData(imageUrls) // Update RecyclerView adapter
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load images.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val imageName = UUID.randomUUID().toString() // Unique image name for Firebase Storage
        val imageRef = storageRef.child(imageName)

        imageRef.putFile(uri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Add the image URL to the list on the UI thread
                imageList.add(downloadUri.toString())
                imageAdapter.notifyItemInserted(imageList.size - 1) // Notify adapter to update RecyclerView
                Toast.makeText(this, "Image added successfully.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeImage(imageUrl: String) {
        // Get image reference from Firebase Storage
        val imageRef = storage.getReferenceFromUrl(imageUrl)

        imageRef.delete().addOnSuccessListener {
            // Remove image URL from list and update RecyclerView
            val index = imageList.indexOf(imageUrl)
            if (index != -1) {
                imageList.removeAt(index)
                imageAdapter.notifyItemRemoved(index) // This will remove the entire CardView from the RecyclerView
                Toast.makeText(this, "Image removed successfully.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to remove image.", Toast.LENGTH_SHORT).show()
        }
    }
}
