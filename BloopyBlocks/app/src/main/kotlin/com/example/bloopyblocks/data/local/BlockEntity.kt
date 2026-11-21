package com.example.bloopyblocks.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocks")
data class BlockEntity (
    @PrimaryKey
    val id: String,
    val name: String,
    val pixels: List<Int>
)

