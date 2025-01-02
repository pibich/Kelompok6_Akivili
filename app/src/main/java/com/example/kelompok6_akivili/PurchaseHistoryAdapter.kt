package com.example.kelompok6_akivili

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class PurchaseHistory(
    val date: String,
    val orderNumber: String,
    val paymentMethod: String,
    val totalAmount: String,
    val status: String,
    val email: String,
    val gameName: String,
    val itemName: String
)

class PurchaseHistoryAdapter(
    private val context: Context,
    private var purchaseHistoryList: MutableList<PurchaseHistory> // Ubah menjadi MutableList
) : RecyclerView.Adapter<PurchaseHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.date)
        val orderNumber: TextView = view.findViewById(R.id.orderNumber)
        val paymentMethod: TextView = view.findViewById(R.id.paymentMethod)
        val totalAmount: TextView = view.findViewById(R.id.totalAmount)
        val email: TextView = view.findViewById(R.id.email)
        val game: TextView = view.findViewById(R.id.game)
        val item: TextView = view.findViewById(R.id.item)
        val orderStatus: TextView = view.findViewById(R.id.orderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_purchase_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val purchase = purchaseHistoryList[position]
        holder.date.text = "Tanggal Pembelian: ${purchase.date}"
        holder.orderNumber.text = "Nomor Pesanan: ${purchase.orderNumber}"
        holder.paymentMethod.text = "Metode Pembayaran: ${purchase.paymentMethod}"
        holder.totalAmount.text = "Total: ${purchase.totalAmount}"
        holder.email.text = "Email: ${purchase.email}"
        holder.game.text = "Game: ${purchase.gameName}"
        holder.item.text = "Item: ${purchase.itemName}"

        holder.orderStatus.text = "Status Pesanan: ${purchase.status}"
        holder.orderStatus.setTextColor(
            if (purchase.status == "Sudah Terkirim") android.graphics.Color.parseColor("#4CAF50")
            else android.graphics.Color.parseColor("#4CAF50")
        )
    }

    override fun getItemCount() = purchaseHistoryList.size

    fun clearItems() {
        purchaseHistoryList.clear()
        notifyDataSetChanged()
    }
}
