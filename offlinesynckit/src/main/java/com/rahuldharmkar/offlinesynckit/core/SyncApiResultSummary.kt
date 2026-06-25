package com.rahuldharmkar.offlinesynckit.core

data class SyncApiResultSummary(
    val success: Boolean,
    val conflict: Boolean = false,
    val finalStatus: SyncStatus
)