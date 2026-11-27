package com.example.bloopyblocks.data
import com.example.bloopyblocks.data.World
import com.example.bloopyblocks.data.local.WorldDao
import com.example.bloopyblocks.data.local.WorldEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomWorldRepository(
    private val worldDao: WorldDao
) : WorldRepository {

    // Retrieve all worlds from Room and convert them to World objects
    override val allWorlds: Flow<List<World>> =
        worldDao.getAllWorlds().map { entities ->
            entities.map { entity ->
                World(
                    id = entity.id,
                    name = entity.name,
                    width = entity.width,
                    height = entity.height,
                    cells = entity.cells
                )
            }
        }

    // Convert World into WorldEntity before saving
    override suspend fun insertWorld(world: World) {
        val entity = WorldEntity(
            id = world.id,
            name = world.name,
            width = world.width,
            height = world.height,
            cells = world.cells
        )

        worldDao.insertWorld(entity)
    }

    // Convert World into WorldEntity before deleting
    override suspend fun deleteWorld(world: World) {
        val entity = WorldEntity(
            id = world.id,
            name = world.name,
            width = world.width,
            height = world.height,
            cells = world.cells
        )

        worldDao.deleteWorld(entity)
    }
}
