package com.example.bloopyblocks.data
import com.example.bloopyblocks.data.local.WorldBlockEntity
import com.example.bloopyblocks.data.local.WorldDao
import com.example.bloopyblocks.data.local.WorldEntity
import kotlinx.coroutines.flow.Flow

class RoomWorldRepository(private val worldDao: WorldDao): WorldRepository {
    override val allWorlds: Flow<List<WorldEntity>> = worldDao.getAllWorlds()
    override suspend fun insertWorld(world: WorldEntity){
        worldDao.insertWorld(world)
    }

    override suspend fun deleteWorld(world: WorldEntity){
        worldDao.deleteWorld(world)
    }
    override suspend fun insertWorldBlock(placement: WorldBlockEntity){
        worldDao.insertWorldBlock(placement)
    }
    override suspend fun deleteWorldBlock(placement: WorldBlockEntity){
        worldDao.deleteWorldBlock(placement)
    }
    override fun getBlocksForWorld(worldId: String): Flow<List<WorldBlockEntity>>{
        return worldDao.getBlocksForWorld(worldId)
    }
}