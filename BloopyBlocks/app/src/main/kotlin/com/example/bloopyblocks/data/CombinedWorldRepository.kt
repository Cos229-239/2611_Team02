package com.example.bloopyblocks.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

class CombinedWorldRepository(
    private val localRepository: WorldRepository,
    private val remoteRepository: WorldRepository
) : WorldRepository {

    override val allWorlds: Flow<List<World>> = flow {

        coroutineScope {

// Listen for worlds from Firestore and copy them into Room
            launch {
                remoteRepository.allWorlds.collect { remoteWorlds ->

                    remoteWorlds.forEach { world ->
                        localRepository.insertWorld(world)
                    }
                }
            }

// Continue observing Room for the UI
            localRepository.allWorlds.collect { localWorlds ->
                emit(localWorlds)
            }
        }
    }

    // Save locally and remotely
    override suspend fun insertWorld(world: World) {
        localRepository.insertWorld(world)
        remoteRepository.insertWorld(world)
    }

    // Delete locally and remotely
    override suspend fun deleteWorld(world: World) {
        localRepository.deleteWorld(world)
        remoteRepository.deleteWorld(world)
    }
}