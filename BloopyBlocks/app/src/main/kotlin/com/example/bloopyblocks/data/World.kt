package com.example.bloopyblocks.data

data class World(
    val id: String = "",
    val name: String = "",
    val width: Int = 12,
    val height: Int = 8,
    val cells: List<String> = emptyList() // flat list, "" means an empty cell
)