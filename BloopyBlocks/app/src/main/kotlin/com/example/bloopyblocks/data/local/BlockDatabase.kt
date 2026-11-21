package com.example.bloopyblocks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BlockEntity::class, WorldEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BlockDatabase : RoomDatabase() {

    abstract fun blockDao(): BlockDao
    abstract fun worldDao(): WorldDao

    companion object {

        @Volatile
        private var database: BlockDatabase? = null

        fun getDatabase(context: Context): BlockDatabase {
            return database ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlockDatabase::class.java,
                    "blocks_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()

                database = instance
                instance
            }
        }

        // Original migration: version 1 -> version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
CREATE TABLE IF NOT EXISTS worlds (
id TEXT NOT NULL,
name TEXT NOT NULL,
PRIMARY KEY(id)
)
""")

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

        // New migration: version 2 -> version 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {

// Create the new worlds table structure
                db.execSQL("""
CREATE TABLE worlds_new (
id TEXT NOT NULL,
name TEXT NOT NULL,
width INTEGER NOT NULL,
height INTEGER NOT NULL,
cells TEXT NOT NULL,
PRIMARY KEY(id)
)
""")

// Preserve existing worlds
// New worlds start with an empty 8x8 grid
                db.execSQL("""
INSERT INTO worlds_new (id, name, width, height, cells)
SELECT id, name, 8, 8, ''
FROM worlds
""")

// Replace the old worlds table
                db.execSQL("DROP TABLE worlds")
                db.execSQL("ALTER TABLE worlds_new RENAME TO worlds")
            }
        }
    }
}


