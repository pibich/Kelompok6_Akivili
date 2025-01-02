package com.example.kelompok6_akivili.model

data class Game(
    val image: String = "", // resource gambar game (gunakan default value)
    val name: String = "", // Nama game (gunakan default value)
    val description: String = "No description available.", // Deskripsi game
    val nominalList: List<Pair<String, String>> = emptyList(), // Daftar nominal
    var id: String = "" // ID game
) {
    // Konstruktor tanpa argumen untuk keperluan deserialisasi Firebase
    constructor() : this("", "", "No description available.", emptyList(), "")
}
