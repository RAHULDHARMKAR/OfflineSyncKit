package com.rahuldharmkar.offlinesynckit.core

data class SyncConflict(
    val queueId: Long,
    val entityName: String,
    val entityId: String,
    val operation: SyncOperation,
    val localPayload: String,
    val serverPayload: String? = null
)