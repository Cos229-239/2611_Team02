package com.example.bloopyblocks.data

data class Block(
    val id: String = "",
    val name: String = "",
    val pixels: List<Int> = emptyList() // 64 colors, row by row, 0 = empty square
) {
    fun isValid() = pixels.size == 64
}