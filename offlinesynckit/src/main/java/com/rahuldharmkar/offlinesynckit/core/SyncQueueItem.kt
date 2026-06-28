package com.rahuldharmkar.offlinesynckit.core

data class SyncQueueItem(
    val id: Long,
    val entityName: String,
    val entityId: String,
    val operation: SyncOperation,
    val tenantId: String? = null,
    val status: SyncStatus,
    val retryCount: Int,
    val lastError: String?,
    val createdAt: Long,
    val updatedAt: Long
)