package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AnchorItem::class],
    version = 2,
    exportSchema = false
)
abstract class AnchorDatabase : RoomDatabase() {
    abstract fun anchorDao(): AnchorDao

    companion object {
        @Volatile
        private var INSTANCE: AnchorDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE anchor_items ADD COLUMN parentId INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_anchor_items_parentId ON anchor_items(parentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_anchor_items_status ON anchor_items(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_anchor_items_dueDate ON anchor_items(dueDate)")
            }
        }

        fun getDatabase(context: Context): AnchorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnchorDatabase::class.java,
                    "anchor_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
