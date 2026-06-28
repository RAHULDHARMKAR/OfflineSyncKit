package com.rahuldharmkar.offlinesynckit.core

data class SyncQueueFilter(
    val status: SyncStatus? = null,
    val entityName: String? = null,
    val operation: SyncOperation? = null,
    val tenantId: String? = null,
    val limit: Int = 100
) {
    init {
        require(limit > 0) {
            "limit must be greater than 0"
        }
    }
}