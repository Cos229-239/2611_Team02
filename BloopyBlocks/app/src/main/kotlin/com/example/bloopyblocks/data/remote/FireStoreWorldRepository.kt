package com.example.bloopyblocks.data.remote
import com.example.bloopyblocks.data.WorldRepository
import com.example.bloopyblocks.data.local.WorldEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import com.example.bloopyblocks.data.local.WorldBlockEntity

class FireStoreWorldRepository(private val firestore: FirebaseFirestore, private val userId: String): WorldRepository{
    private val worldCollection = firestore.collection("users").document(userId).collection("worlds")
    override val allWorlds: Flow<List<WorldEntity>> = callbackFlow {
        val listener = worldCollection.addSnapshotListener { snapshots, exception ->
            if(exception!=null){
                close(exception)
            }
            else{
                val worlds = snapshots?.documents?.mapNotNull {
                    document->documentToWorld(document)
                }
                    ?: emptyList()
                trySend(worlds)
            }
        }
        awaitClose {
            listener.remove()
        }
    }

   override suspend fun insertWorld(world: WorldEntity){

    }
    override fun deleteWorld(world: WorldEntity){

    }
    override suspend fun insertWorldBlock(placement: WorldBlockEntity){

    }
    override suspend fun deleteWorldBlock(placement: WorldBlockEntity){

    }
    override fun getBlocksForWorld(worldId: String): Flow<List<WorldBlockEntity>>{

    }

}