package com.example.bloopyblocks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worlds")
data class WorldEntity(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val width: Int = 8,
    val height: Int = 8,
    val cells: List<String> = emptyList()
)
