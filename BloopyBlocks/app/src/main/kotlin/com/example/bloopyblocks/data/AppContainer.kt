package com.example.bloopyblocks.data
import android.content.Context
import com.example.bloopyblocks.data.local.BlockDatabase
import com.example.bloopyblocks.data.remote.FireBaseAuthManager
import com.example.bloopyblocks.data.remote.FireStoreBlockRepository
import com.example.bloopyblocks.data.remote.FireStoreWorldRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private var blockRepository: BlockRepository?=null
    private var worldRepository: WorldRepository?=null
    suspend fun getBlockRepository(): BlockRepository {
        if (blockRepository == null) {


            blockRepository = withContext(Dispatchers.IO) {
                val database = BlockDatabase.getDatabase(appContext)
                val localRepository = RoomBlockRepository(database.blockDao())
                val userId = FireBaseAuthManager().getUserId()
                val RemoteRepository = FireStoreBlockRepository(
                    com.google.firebase.firestore.FirebaseFirestore.getInstance(),
                    userId
                )
                CombinedBlockRepository(localRepository, RemoteRepository)

            }


        }
        return blockRepository!!
    }
    suspend fun getWorldRepository(): WorldRepository{
        if(worldRepository==null){
            worldRepository = withContext(Dispatchers.IO){
                val database = BlockDatabase.getDatabase(appContext)
                val localRepository = RoomWorldRepository(database.worldDao())
                val userId = FireBaseAuthManager().getUserId()
                val RemoteRepository = FireStoreWorldRepository(
                    com.google.firebase.firestore.FirebaseFirestore.getInstance(),
                    userId
                )
                CombinedWorldRepository(localRepository, RemoteRepository)
            }
        }
        return worldRepository!!
    }
}