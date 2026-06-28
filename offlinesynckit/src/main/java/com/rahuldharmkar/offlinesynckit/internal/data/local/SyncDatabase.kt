package com.rahuldharmkar.offlinesynckit.internal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [SyncQueueEntity::class, SyncStateEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(SyncConverters::class)
internal abstract class SyncDatabase : RoomDatabase() {

    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun syncStateDao(): SyncStateDao

    companion object {
        @Volatile
        private var INSTANCE: SyncDatabase? = null

        fun getInstance(context: Context): SyncDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SyncDatabase::class.java,
                    "offline_sync_kit.db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}