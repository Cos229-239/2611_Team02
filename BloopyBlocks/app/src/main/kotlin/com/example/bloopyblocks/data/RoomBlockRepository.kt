package com.example.bloopyblocks.data
import com.example.bloopyblocks.data.local.BlockDao
import com.example.bloopyblocks.data.local.BlockEntity
import kotlinx.coroutines.flow.Flow

class RoomBlockRepository(private val blockDao: BlockDao): BlockRepository {
    override val allBlocks: Flow<List<BlockEntity>> = blockDao.getAllBlocks()
    override suspend fun insertBlock(block: BlockEntity){
        blockDao.insertBlock(block)
    }
    override suspend fun updateBlock(block: BlockEntity){
        blockDao.updateBlock(block)
    }
    override suspend fun deleteBlock(block: BlockEntity){
        blockDao.deleteBlock(block)
    }
    override suspend fun getBlockById(id: String): BlockEntity?{
        return blockDao.getBlockById(id)
    }

}