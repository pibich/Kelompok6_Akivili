package com.example.kelompok6_akivili.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.example.kelompok6_akivili.R

class ImageCarouselAdapter(
    private val context: Context,
    private val imageUrls: List<String>
) : PagerAdapter() {

    private var currentPosition = 0
    private lateinit var dotsLayout: LinearLayout

    // You can set dotsLayout later through a setter function
    fun setDotsLayout(dotsLayout: LinearLayout) {
        this.dotsLayout = dotsLayout
        updateDots() // Update dots layout after it is set
    }

    override fun getCount(): Int {
        return imageUrls.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_carousel, container, false)

        val imageView = view.findViewById<ImageView>(R.id.carouselImageView)

        // Load image from URL
        Glide.with(context)
            .load(imageUrls[position])
            .placeholder(R.drawable.placeholder)  // Placeholder during loading
            .error(R.drawable.error_image)        // Error image if failed to load
            .into(imageView)

        container.addView(view)

        // Update position and dots
        currentPosition = position
        updateDots()

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    // Method to update dots based on the current position
    private fun updateDots() {
        // Check if dotsLayout is initialized
        if (::dotsLayout.isInitialized) {
            dotsLayout.removeAllViews()

            for (i in 0 until imageUrls.size) {
                val dot = ImageView(context)
                val params = LinearLayout.LayoutParams(16, 16)
                params.setMargins(8, 0, 8, 0)
                dot.layoutParams = params

                if (i == currentPosition) {
                    dot.setImageResource(R.drawable.dot_active)
                } else {
                    dot.setImageResource(R.drawable.dot_inactive)
                }

                dotsLayout.addView(dot)
            }
        }
    }

}
