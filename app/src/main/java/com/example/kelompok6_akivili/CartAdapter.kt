package com.example.kelompok6_akivili.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kelompok6_akivili.R
import CartItem

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<CartItem>,
    private val onCartUpdate: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.cartItemImage)
        val nameTextView: TextView = view.findViewById(R.id.cartItemName)
        val priceTextView: TextView = view.findViewById(R.id.cartItemPrice)
        val quantityTextView: TextView = view.findViewById(R.id.cartItemQuantity)
        val plusButton: ImageButton = view.findViewById(R.id.cartItemPlus)
        val minusButton: ImageButton = view.findViewById(R.id.cartItemMinus)
        val deleteButton: ImageButton = view.findViewById(R.id.cartItemDelete)
        val checkBox: CheckBox = view.findViewById(R.id.cartItemCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        // Memeriksa jika image adalah URL dan memuatnya menggunakan Glide
        if (item.image.startsWith("http")) {
            // If item.image is a URL, load it using Glide
            Glide.with(context)
                .load(item.image)
                .into(holder.imageView)
        } else {
            // If item.image is an Int (resource ID), use setImageResource
            holder.imageView.setImageResource(item.image.toInt()) // Cast the string to int resource ID
        }

        holder.nameTextView.text = item.name
        holder.priceTextView.text = formatPrice(item.price * item.quantity)
        holder.quantityTextView.text = item.quantity.toString()
        holder.checkBox.isChecked = item.isSelected

        holder.plusButton.setOnClickListener {
            item.quantity++
            notifyItemChanged(position)
            onCartUpdate()
        }

        holder.minusButton.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
                onCartUpdate()
            }
        }

        holder.deleteButton.setOnClickListener {
            cartItems.removeAt(position)

            // Cek apakah ada item tersisa
            if (cartItems.isEmpty()) {
                // Jika kosong, refresh seluruh RecyclerView
                notifyDataSetChanged()
            } else {
                // Jika masih ada item, hapus sesuai posisi
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartItems.size)  // Update posisi item lainnya
            }

            onCartUpdate()
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            onCartUpdate()
        }
    }

    // Format price with "Rp" and thousand separators
    private fun formatPrice(price: Int): String {
        val decimalFormat = java.text.DecimalFormat("#,###")
        return "Rp ${decimalFormat.format(price)}"
    }

    override fun getItemCount(): Int = cartItems.size
}
