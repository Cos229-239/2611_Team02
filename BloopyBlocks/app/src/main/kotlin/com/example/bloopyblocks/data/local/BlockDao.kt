package com.example.bloopyblocks.data.local
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    @Insert
    suspend fun insertBlock(block: BlockEntity)
    @Update
    suspend fun updateBlock(block: BlockEntity)
    @Delete
    suspend fun deleteBlock(block: BlockEntity)
    @Query("SELECT * FROM blocks")
    fun getAllBlocks(): Flow<List<BlockEntity>>
    @Query("SELECT * FROM blocks WHERE id = :id")
    suspend fun getBlockById(id: String): BlockEntity?

}