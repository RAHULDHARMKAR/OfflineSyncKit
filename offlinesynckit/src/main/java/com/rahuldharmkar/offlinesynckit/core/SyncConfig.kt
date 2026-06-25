package com.rahuldharmkar.offlinesynckit.core

data class SyncConfig(
    val retryPolicy: SyncRetryPolicy = SyncRetryPolicy(),
    val syncBatchSize: Int = 20,
    val enablePeriodicSync: Boolean = false,
    val periodicSyncIntervalMinutes: Long = 15L,
    val autoSyncWhenOnline: Boolean = true,
    val mergePolicy: SyncMergePolicy = SyncMergePolicy.APPEND_ONLY,
    val eventListener: SyncEventListener? = null,
    val logger: SyncLogger? = null,
    val staleSyncingTimeoutMinutes: Long = 10L,
    val autoClearSyncedItems: Boolean = false,
    val syncedItemRetentionMinutes: Long = 60L,
    val syncPaused: Boolean = false,
    val syncDirection: SyncDirection = SyncDirection.PUSH,
    val conflictStrategy: SyncConflictStrategy = SyncConflictStrategy.MANUAL,
    val conflictResolver: SyncConflictResolver? = null,
    val serializerRegistry: SyncSerializerRegistry = SyncSerializerRegistry(),
    val interceptors: List<SyncInterceptor> = emptyList()
) {
    init {
        require(syncBatchSize > 0) {
            "syncBatchSize must be greater than 0"
        }

        require(periodicSyncIntervalMinutes >= 15L) {
            "periodicSyncIntervalMinutes must be at least 15 minutes"
        }

        require(staleSyncingTimeoutMinutes > 0) {
            "staleSyncingTimeoutMinutes must be greater than 0"
        }

        require(syncedItemRetentionMinutes > 0) {
            "syncedItemRetentionMinutes must be greater than 0"
        }
    }
}