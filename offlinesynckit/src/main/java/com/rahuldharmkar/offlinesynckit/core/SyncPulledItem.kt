package com.rahuldharmkar.offlinesynckit.core

/**
 * Represents one item pulled from the remote server.
 */
data class SyncPulledItem(
    val entityName: String,
    val entityId: String,
    val operation: SyncOperation,
    val payload: String,
    val updatedAt: Long,
    val tenantId: String? = null,
    val metadata: Map<String, String> = emptyMap()
)