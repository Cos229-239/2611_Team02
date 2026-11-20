package com.example.bloopyblocks.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BlockEntity::class, WorldEntity::class, WorldBlockEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BlockDatabase:RoomDatabase() {
    abstract fun blockDao():BlockDao
    abstract fun worldDao(): WorldDao
    companion object{
        @Volatile
        private var database: BlockDatabase?=null
        fun getDatabase(context: Context): BlockDatabase{
            return database ?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext, BlockDatabase::class.java, "blocks_database").addMigrations(MIGRATION_1_2).build()
                database = instance
                instance
            }
        }
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                // Create the worlds table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS worlds (
                    id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    PRIMARY KEY(id)
                    )
                """)

                // Create the world_block table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS world_block (
                    id TEXT NOT NULL,
                    worldId TEXT NOT NULL,
                    blockId TEXT NOT NULL,
                    x REAL NOT NULL,
                    y REAL NOT NULL,
                    PRIMARY KEY(id)
                    )
                """)
            }
        }
    }
}