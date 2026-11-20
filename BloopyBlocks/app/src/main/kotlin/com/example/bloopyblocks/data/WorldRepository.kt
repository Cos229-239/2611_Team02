package com.example.bloopyblocks.data
import com.example.bloopyblocks.data.local.WorldEntity
import com.example.bloopyblocks.data.local.WorldBlockEntity
import kotlinx.coroutines.flow.Flow
interface WorldRepository {
    val allWorlds: Flow<List<WorldEntity>>
    suspend fun insertWorld(world: WorldEntity)
    suspend fun deleteWorld(world: WorldEntity)
    suspend fun insertWorldBlock(placement: WorldBlockEntity)
    suspend fun deleteWorldBlock(placement: WorldBlockEntity)
    fun getBlocksForWorld(worldId: String): Flow<List<WorldBlockEntity>>

}