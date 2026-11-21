package com.example.bloopyblocks.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldDao {

    @Insert
    suspend fun insertWorld(world: WorldEntity)

    @Update
    suspend fun updateWorld(world: WorldEntity)

    @Delete
    suspend fun deleteWorld(world: WorldEntity)

    @Query("SELECT * FROM worlds")
    fun getAllWorlds(): Flow<List<WorldEntity>>

    @Query("SELECT * FROM worlds WHERE id = :id")
    suspend fun getWorldById(id: String): WorldEntity?
}
