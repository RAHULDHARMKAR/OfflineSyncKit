package com.rahuldharmkar.offlinesynckit.core

/**
 * Handles pulled remote items so the host app can apply them
 * to its own local database or cache.
 */
fun interface SyncPullDataHandler {

    /**
     * Called after pulled items are persisted inside OfflineSyncKit
     * but before the next sync token is saved.
     */
    suspend fun onItemsPulled(
        items: List<SyncPulledItem>
    )
}