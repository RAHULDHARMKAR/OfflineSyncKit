package com.rahuldharmkar.offlinesynckit.core

/**
 * Request sent to the pull adapter when OfflineSyncKit asks the server
 * for remote changes.
 */
data class SyncPullRequest(
    val lastSyncToken: String? = null,
    val updatedAfter: Long? = null,
    val tenantId: String? = null,
    val limit: Int = 100
) {
    init {
        require(limit > 0) {
            "limit must be greater than 0"
        }
    }
}