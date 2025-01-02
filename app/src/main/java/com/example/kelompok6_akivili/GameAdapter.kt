package com.example.kelompok6_akivili.adapter

import android.content.Context  // Import Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.kelompok6_akivili.R
import com.example.kelompok6_akivili.model.Game
import com.google.firebase.storage.FirebaseStorage

class GameAdapter(
    private val context: Context,  // Pass the context as Context, not MutableList<Game>
    private var gameList: List<Game>,  // The list of games should be a List<Game>
    private val onClick: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = gameList[position]

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        // Check if the image URL starts with gs:// (Firebase Storage)
        if (game.image.startsWith("gs://")) {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(game.image)
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(context)
                    .setDefaultRequestOptions(requestOptions)
                    .load(uri)  // Use URI from Firebase storage
                    .thumbnail(0.1f)
                    .into(holder.gameImageView)
            }.addOnFailureListener {
                holder.gameImageView.setImageResource(R.drawable.error_image)
            }
        } else {
            // For other image URLs (e.g., from remote server)
            Glide.with(context)
                .load(game.image)
                .apply(requestOptions)
                .into(holder.gameImageView)
        }

        holder.gameNameTextView.text = game.name
        holder.itemView.setOnClickListener {
            onClick(game)
        }
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    // Update the list and notify the adapter
    fun updateList(newList: List<Game>) {
        gameList = newList  // Update the gameList with the new list
        notifyDataSetChanged()  // Notify the adapter of the update
    }

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameImageView: ImageView = itemView.findViewById(R.id.gameImageView)
        val gameNameTextView: TextView = itemView.findViewById(R.id.gameNameTextView)
    }
}
