package com.rahuldharmkar.offlinesynckit.api

import com.rahuldharmkar.offlinesynckit.core.SyncPullRequest
import com.rahuldharmkar.offlinesynckit.core.SyncPullResult

/**
 * Adapter responsible for pulling remote changes from the server.
 */
fun interface SyncPullAdapter {

    /**
     * Pulls remote changes from the backend.
     */
    suspend fun pull(
        request: SyncPullRequest
    ): SyncPullResult
}