package com.example.bloopyblocks.data
import com.example.bloopyblocks.data.local.BlockEntity
import kotlinx.coroutines.flow.Flow

class CombinedBlockRepository(private val localRepository: BlockRepository, private val RemoteRepository: BlockRepository): BlockRepository {
    override suspend fun insertBlock(block: BlockEntity) {
        localRepository.insertBlock(block)
        RemoteRepository.insertBlock(block)
    }

    override suspend fun deleteBlock(block: BlockEntity) {
        localRepository.deleteBlock(block)
        RemoteRepository.deleteBlock(block)
    }

    override suspend fun updateBlock(block: BlockEntity){
        localRepository.updateBlock(block)
        RemoteRepository.updateBlock(block)
    }

    override suspend fun getBlockById(id: String): BlockEntity?{
        return localRepository.getBlockById(id)
    }

    override val allBlocks: Flow<List<BlockEntity>> = localRepository.allBlocks
}