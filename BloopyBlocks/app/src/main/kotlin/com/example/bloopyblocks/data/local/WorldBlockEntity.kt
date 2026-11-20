package com.example.bloopyblocks.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_block")
data class WorldBlockEntity (
    @PrimaryKey
    val id: String,
    val worldId: String,
    val blockId: String,
    val x: Float,
    val y: Float
)



