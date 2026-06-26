package com.rahuldharmkar.offlinesynckit.core

data class SyncRequest(
    val entityName: String,
    val entityId: String,
    val operation: SyncOperation,
    val payload: String,
    val authToken: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val retryCount: Int = 0,
    val queuedAt: Long = 0L,
    val updatedAt: Long = 0L,
    val metadata: Map<String, String> = emptyMap()
)