package com.rahuldharmkar.offlinesynckit.internal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface SyncStateDao {

    @Query("SELECT * FROM sync_state WHERE key = :key LIMIT 1")
    suspend fun getState(key: String): SyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SyncStateEntity)
}