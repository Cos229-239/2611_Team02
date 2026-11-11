package com.example.bloopyblocks.data

data class Block(
    val id: String = "",
    val name: String = "",
    val palette: List<String> = emptyList(),
    val pixels: List<Int> = emptyList()
) {
    fun isValid() = pixels.size == 64
}