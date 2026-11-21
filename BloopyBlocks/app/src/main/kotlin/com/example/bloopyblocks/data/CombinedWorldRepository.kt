package com.example.bloopyblocks.data

import kotlinx.coroutines.flow.Flow

class CombinedWorldRepository(
    private val localRepository: WorldRepository,
    private val remoteRepository: WorldRepository
) : WorldRepository {

    override val allWorlds: Flow<List<World>> =
        localRepository.allWorlds

    override suspend fun insertWorld(world: World) {
        localRepository.insertWorld(world)
        remoteRepository.insertWorld(world)
    }

    override suspend fun deleteWorld(world: World) {
        localRepository.deleteWorld(world)
        remoteRepository.deleteWorld(world)
    }
}