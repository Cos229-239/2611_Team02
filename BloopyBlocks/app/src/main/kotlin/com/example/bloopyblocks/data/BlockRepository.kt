package com.example.bloopyblocks.data
import com.example.bloopyblocks.data.local.BlockEntity
import kotlinx.coroutines.flow.Flow

interface BlockRepository {
    val allBlocks: Flow<List<BlockEntity>>
    suspend fun insertBlock(block: BlockEntity)
    suspend fun updateBlock(block: BlockEntity)
    suspend fun deleteBlock(block: BlockEntity)
    suspend fun getBlockById(id: String): BlockEntity?
}