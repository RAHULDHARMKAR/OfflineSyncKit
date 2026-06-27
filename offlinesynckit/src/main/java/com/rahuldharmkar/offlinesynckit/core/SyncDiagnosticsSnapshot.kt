package com.rahuldharmkar.offlinesynckit.core

/**
 * Developer-friendly diagnostic snapshot of the current OfflineSyncKit configuration
 * and runtime state.
 */
data class SyncDiagnosticsSnapshot(
    val syncDirection: SyncDirection,
    val isSyncPaused: Boolean,
    val isPolicyAllowed: Boolean,
    val autoSyncWhenOnline: Boolean,
    val enablePeriodicSync: Boolean,
    val periodicSyncIntervalMinutes: Long,
    val syncBatchSize: Int,
    val maxRetryCount: Int,
    val autoClearSyncedItems: Boolean,
    val syncedItemRetentionMinutes: Long,
    val staleSyncingTimeoutMinutes: Long,
    val totalQueueItems: Int,
    val pendingCount: Int,
    val syncingCount: Int,
    val syncedCount: Int,
    val failedCount: Int,
    val conflictCount: Int,
    val giveUpCount: Int
)