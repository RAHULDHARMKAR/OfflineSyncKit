package com.rahuldharmkar.offlinesynckit.core

/**
 * Snapshot of OfflineSyncKit health and queue state.
 */
data class SyncHealthReport(
    val totalQueueItems: Int,
    val pendingCount: Int,
    val syncingCount: Int,
    val syncedCount: Int,
    val failedCount: Int,
    val conflictCount: Int,
    val giveUpCount: Int,
    val isSyncPaused: Boolean,
    val syncDirection: SyncDirection,
    val isPolicyAllowed: Boolean
)