package com.example.kelompok6_akivili.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kelompok6_akivili.R
import com.example.kelompok6_akivili.model.Game

class GameAdapter(private val context: Context, private var gameList: List<Game>) :
    RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = gameList[position]
        holder.gameImageView.setImageResource(game.image)
        holder.gameNameTextView.text = game.name

        // Prevent name from changing layout size by limiting text lines and truncating
        holder.gameNameTextView.maxLines = 1
        holder.gameNameTextView.ellipsize = android.text.TextUtils.TruncateAt.END
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    fun updateList(newList: List<Game>) {
        gameList = newList
        notifyDataSetChanged()
    }

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameImageView: ImageView = itemView.findViewById(R.id.gameImageView)
        val gameNameTextView: TextView = itemView.findViewById(R.id.gameNameTextView)
    }
}
