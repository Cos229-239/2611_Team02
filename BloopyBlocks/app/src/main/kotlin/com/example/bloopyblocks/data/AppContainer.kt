package com.example.bloopyblocks.data
import android.content.Context
import com.example.bloopyblocks.data.local.BlockDatabase
import com.example.bloopyblocks.data.remote.FireBaseAuthManager
import com.example.bloopyblocks.data.remote.FireStoreBlockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    suspend fun CreateBlockRepository(): BlockRepository =
        withContext(Dispatchers.IO){
            val database = BlockDatabase.getDatabase(appContext)
            val localRepository = RoomBlockRepository(database.blockDao())
            val userId = FireBaseAuthManager().getUserId()
            val RemoteRepository = FireStoreBlockRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance(), userId)
            CombinedBlockRepository(localRepository, RemoteRepository)

        }
}