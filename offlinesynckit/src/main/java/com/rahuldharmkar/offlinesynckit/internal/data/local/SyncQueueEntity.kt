package com.rahuldharmkar.offlinesynckit.internal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rahuldharmkar.offlinesynckit.core.SyncOperation


@Entity(tableName = "sync_queue")
internal data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val entityName: String,
    val entityId: String,

    val operation: SyncOperation,
    val payload: String,
    val tenantId: String? = null,
    val status: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING,
    val retryCount: Int = 0,
    val lastError: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

)