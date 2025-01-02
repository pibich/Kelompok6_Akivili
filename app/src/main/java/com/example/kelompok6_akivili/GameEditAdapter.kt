package com.example.kelompok6_akivili.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.kelompok6_akivili.EditGameActivity
import com.example.kelompok6_akivili.R
import com.example.kelompok6_akivili.model.Game
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class GameEditAdapter(
    private val context: Context,
    private var gameList: List<Game>,  // List game yang akan ditampilkan
    private val onClick: (Game) -> Unit  // Listener untuk klik item game
) : RecyclerView.Adapter<GameEditAdapter.GameViewHolder>() {

    // onCreateViewHolder digunakan untuk membuat tampilan setiap item game
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_game_list_admin, parent, false)
        return GameViewHolder(view)
    }

    // onBindViewHolder mengikat data ke tampilan setiap item game
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = gameList[position]
        Log.d("GameEditAdapter", "Binding game: ${game.name}")

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        // Memeriksa apakah URL gambar dimulai dengan gs:// (Firebase Storage)
        if (game.image.startsWith("gs://")) {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(game.image)
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(context)
                    .setDefaultRequestOptions(requestOptions)
                    .load(uri)  // Menggunakan URI dari Firebase Storage
                    .thumbnail(0.1f)
                    .into(holder.gameImageView)
            }.addOnFailureListener {
                holder.gameImageView.setImageResource(R.drawable.error_image)
            }
        } else {
            // Untuk URL gambar lainnya (misalnya dari server)
            Glide.with(context)
                .load(game.image)
                .apply(requestOptions)
                .into(holder.gameImageView)
        }

        // Menampilkan nama game di TextView
        holder.gameNameTextView.text = game.name

        // Menangani klik pada item game
        holder.itemView.setOnClickListener {
            // Kirim data game ke EditGameActivity
            val intent = Intent(context, EditGameActivity::class.java)
            intent.putExtra("gameName", game.name)
            intent.putExtra("gameDescription", game.description)
            intent.putExtra("gameImage", game.image)
            intent.putExtra("gameId", game.id)
            intent.putExtra("nominalList", ArrayList(game.nominalList))  // Mengirim nominalList sebagai ArrayList
            context.startActivity(intent)
            Toast.makeText(context, "Game clicked: ${game.name}", Toast.LENGTH_SHORT).show()
        }

        // Menambahkan listener untuk removeIcon
        holder.removeIcon.setOnClickListener {
            val gameName = holder.gameNameTextView.text.toString()  // Ambil nama game
            removeGameFromFirestoreAndStorage(gameName)
        }
    }

    // getItemCount mengembalikan jumlah item dalam list game
    override fun getItemCount(): Int {
        return gameList.size
    }

    // Fungsi untuk memperbarui daftar game
    fun updateList(newList: List<Game>) {
        gameList = newList  // Memperbarui gameList dengan daftar baru
        notifyDataSetChanged()  // Memberitahukan adapter bahwa data sudah diperbarui
    }

    // ViewHolder untuk menyimpan tampilan setiap item game
    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameImageView: ImageView = itemView.findViewById(R.id.gameImageView)
        val gameNameTextView: TextView = itemView.findViewById(R.id.gameNameTextView)
        val removeIcon: ImageView = itemView.findViewById(R.id.removeIcon)  // Icon untuk remove
    }

    // Fungsi untuk menghapus game dari Firestore dan Firebase Storage
    private fun removeGameFromFirestoreAndStorage(gameName: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Menghapus data game dari Firestore
        firestore.collection("Games").document(gameName).delete()
            .addOnSuccessListener {
                // Hapus gambar dari Firebase Storage
                val storageRef = FirebaseStorage.getInstance().reference.child("Games/$gameName.jpg")
                storageRef.delete().addOnSuccessListener {
                    Toast.makeText(context, "Game and image deleted successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception ->
                    Log.d("GameEditActivity", "Failed to delete image: ${exception.message}")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to delete game: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk membuka activity untuk mengedit game
    private fun openEditGameActivity(gameName: String) {
        val intent = Intent(context, EditGameActivity::class.java)
        intent.putExtra("gameName", gameName)  // Kirim nama game yang akan diedit
        context.startActivity(intent)
    }

    // Fungsi untuk mengambil data game dari Firestore
    fun fetchGamesData() {
        val firestore = FirebaseFirestore.getInstance()
        val gamesCollection = firestore.collection("Games")

        gamesCollection.get()
            .addOnSuccessListener { documents ->
                val games = mutableListOf<Game>()

                for (document in documents) {
                    val gameName = document.getString("name") ?: ""
                    val gameDescription = document.getString("description") ?: ""
                    val gameImage = document.getString("imageUrl") ?: ""
                    val nominalList = document.get("nominalList") as? List<Map<String, String>> ?: emptyList()
                    val gameId = document.getString("id") ?: ""

                    // Mengonversi nominalList menjadi List<Pair<String, String>?>
                    val formattedNominalList = nominalList.mapNotNull { map ->
                        val nominal = map["nominal"]
                        val price = map["price"]
                        if (nominal != null && price != null) {
                            Pair(nominal, price)  // Membuat Pair dari nama nominal dan harga
                        } else {
                            null  // Jika ada data yang tidak lengkap, abaikan item ini
                        }
                    }

                    val game = Game(
                        name = gameName,
                        description = gameDescription,
                        image = gameImage,
                        nominalList = formattedNominalList,
                        id = gameId
                    )

                    Log.d("GameEditActivity", "gameId: $gameId")

                    games.add(game)
                }

                updateList(games)  // Memperbarui adapter dengan data game yang diambil
                Log.d("GameEditActivity", "Jumlah game yang diterima: ${games.size}")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.d("GameEditActivity", "Error fetching data: ${exception.message}")
            }
    }
}
