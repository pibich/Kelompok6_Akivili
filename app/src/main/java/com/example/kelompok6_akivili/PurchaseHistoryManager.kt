import android.content.Context
import com.example.kelompok6_akivili.PurchaseHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PurchaseHistoryManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("purchase_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun savePurchase(purchase: PurchaseHistory) {
        val existingList = getAllPurchases().toMutableList()
        val isDuplicate = existingList.any { it.orderNumber == purchase.orderNumber }

        if (!isDuplicate) {
            existingList.add(0, purchase)  // Menambahkan ke paling atas
            val jsonString = gson.toJson(existingList)
            sharedPreferences.edit().putString("purchases", jsonString).apply()
        }
    }

    fun getAllPurchases(): List<PurchaseHistory> {
        val jsonString = sharedPreferences.getString("purchases", null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<PurchaseHistory>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyList()
        }
    }

    fun clearHistory() {
        sharedPreferences.edit().remove("purchases").apply()
    }
}
