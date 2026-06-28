package com.rahuldharmkar.offlinesynckit.internal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_state")
internal data class SyncStateEntity(
    @PrimaryKey
    val key: String,
    val lastSyncToken: String? = null,
    val lastPulledAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)