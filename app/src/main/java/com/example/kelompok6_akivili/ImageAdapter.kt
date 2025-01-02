package com.example.kelompok6_akivili.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kelompok6_akivili.R

class ImageAdapter(
    private var imageList: MutableList<String>, // Change to MutableList to allow modification
    private val context: Context,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view_pager_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        Glide.with(context).load(imageUrl).into(holder.carouselImageView)

        // Handle image click (to remove)
        holder.removeIcon.setOnClickListener {
            onImageClick(imageUrl)
            Glide.with(context).clear(holder.carouselImageView) // Optionally clear the image from Glide cache
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun updateData(newData: List<String>) {
        imageList.clear()
        imageList.addAll(newData)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val carouselImageView: ImageView = itemView.findViewById(R.id.carouselImageView)
        val removeIcon: ImageView = itemView.findViewById(R.id.removeIcon)
    }
}
