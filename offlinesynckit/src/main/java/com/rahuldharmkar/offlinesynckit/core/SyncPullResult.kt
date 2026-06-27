package com.rahuldharmkar.offlinesynckit.core

/**
 * Result returned by a pull synchronization request.
 */
data class SyncPullResult(
    val success: Boolean,
    val items: List<SyncPulledItem> = emptyList(),
    val nextSyncToken: String? = null,
    val errorMessage: String? = null
)