import android.os.Parcel
import android.os.Parcelable

data class CartItem(
    val image: String,
    val name: String,
    val price: Int,
    var quantity: Int = 1,
    var isSelected: Boolean = false,
    val nominalList: List<Pair<String, String>> // Menambahkan properti nominalList
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // Pastikan kita membaca String dengan benar
        parcel.readString() ?: "", // pastikan membaca String dengan benar
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        // Membaca nominalList sebagai List<Pair<String, String>> dengan cara yang benar
        mutableListOf<Pair<String, String>>().apply {
            // Membaca list Pair<String, String> dari parcel
            parcel.readList(this, Pair::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(image)
        parcel.writeString(name)
        parcel.writeInt(price)
        parcel.writeInt(quantity)
        parcel.writeByte(if (isSelected) 1 else 0)
        // Menulis nominalList ke parcel secara benar
        parcel.writeList(nominalList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CartItem> {
        override fun createFromParcel(parcel: Parcel): CartItem {
            return CartItem(parcel)
        }

        override fun newArray(size: Int): Array<CartItem?> {
            return arrayOfNulls(size)
        }
    }
}
