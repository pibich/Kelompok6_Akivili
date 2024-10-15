package com.example.kelompok6_akivili

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class PurchaseHistory(val itemName: String, val date: String)

class PurchaseHistoryAdapter(private val context: Context, private val purchaseHistoryList: List<PurchaseHistory>) : RecyclerView.Adapter<PurchaseHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.itemName)
        val date: TextView = view.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_purchase_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val purchaseHistory = purchaseHistoryList[position]
        holder.itemName.text = purchaseHistory.itemName
        holder.date.text = purchaseHistory.date
    }

    override fun getItemCount() = purchaseHistoryList.size
}
