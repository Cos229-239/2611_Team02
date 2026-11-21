package com.example.bloopyblocks.data.remote
import com.example.bloopyblocks.data.BlockRepository
import com.example.bloopyblocks.data.local.BlockEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot

class FireStoreBlockRepository(private val firestore: FirebaseFirestore, private val userId: String): BlockRepository{
    private val blocksCollection = firestore.collection("users").document(userId).collection("blocks")
    override suspend fun insertBlock(block: BlockEntity){
        blocksCollection.document(block.id).set(block).await()

    }
    override suspend fun updateBlock(block: BlockEntity){
        blocksCollection.document(block.id).set(block).await()


    }

    override suspend fun deleteBlock(block: BlockEntity){
        blocksCollection.document(block.id).delete().await()

    }

    override suspend fun getBlockById(id: String): BlockEntity?{
        val document = blocksCollection.document(id).get().await()
        return if(document.exists()){
            documentToBlock(document)

        }
        else{
            null
        }
    }

    private fun documentToBlock(document: DocumentSnapshot): BlockEntity?{
        val name = document.getString("name")?:return null
        val pixels = document.get("pixels") as? List<*> ?: return null
        return BlockEntity(id = document.id, name = name, pixels = pixels.map{(it as Number).toInt()})
    }

    override val allBlocks: Flow<List<BlockEntity>> = callbackFlow {
        val listener = blocksCollection.addSnapshotListener { snapshots, exception ->
            if(exception != null){
                close(exception)
            }
            else{
                val blocks = snapshots?.documents?.mapNotNull {
                    document->
                   documentToBlock(document)

                }?: emptyList()
                trySend(blocks)


            }
        }
        awaitClose {
            listener.remove()
        }
    }

}