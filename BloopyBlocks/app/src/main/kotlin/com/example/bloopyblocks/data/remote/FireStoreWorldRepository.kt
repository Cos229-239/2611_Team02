package com.example.bloopyblocks.data.remote

import com.example.bloopyblocks.data.World
import com.example.bloopyblocks.data.WorldRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FireStoreWorldRepository(
    private val firestore: FirebaseFirestore,
    private val userId: String
) : WorldRepository {

    private val worldCollection = firestore
        .collection("users")
        .document(userId)
        .collection("worlds")

    // Listen for changes to the user's saved worlds
    override val allWorlds: Flow<List<World>> = callbackFlow {

        val listener = worldCollection.addSnapshotListener { snapshots, exception ->

            if (exception != null) {
                close(exception)
            } else {

                val worlds = snapshots?.documents?.mapNotNull { document ->
                    documentToWorld(document)
                } ?: emptyList()

                trySend(worlds)
            }
        }

        awaitClose {
            listener.remove()
        }
    }

    // Save an entire world, including its grid cells
    override suspend fun insertWorld(world: World) {
        worldCollection
            .document(world.id)
            .set(world)
            .await()
    }

    // Delete an entire world
    override suspend fun deleteWorld(world: World) {
        worldCollection
            .document(world.id)
            .delete()
            .await()
    }

    // Convert a Firestore document into a World object
    private fun documentToWorld(document: DocumentSnapshot): World? {

        val name = document.getString("name") ?: return null

        val width = (document.getLong("width") ?: 8L).toInt()

        val height = (document.getLong("height") ?: 8L).toInt()

        val cells = (document.get("cells") as? List<*>)
            ?.mapNotNull { it as? String }
            ?: emptyList()

        return World(
            id = document.id,
            name = name,
            width = width,
            height = height,
            cells = cells
        )
    }
}