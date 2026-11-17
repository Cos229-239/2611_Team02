package com.example.bloopyblocks.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(
    entities = [BlockEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BlockDatabase:RoomDatabase() {
    abstract fun blockDao():BlockDao
    companion object{
        @Volatile
        private var database: BlockDatabase?=null
        fun getDatabase(context: Context): BlockDatabase{
            return database ?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext, BlockDatabase::class.java, "blocks_database").build()
                database = instance
                instance
            }
        }
    }
}