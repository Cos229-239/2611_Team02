package com.example.bloopyblocks.data


import kotlinx.coroutines.flow.Flow

interface WorldRepository {

    val allWorlds: Flow<List<World>>

    suspend fun insertWorld(world: World)

    suspend fun deleteWorld(world: World)
}