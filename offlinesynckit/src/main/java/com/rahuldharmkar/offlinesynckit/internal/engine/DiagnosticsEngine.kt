package com.rahuldharmkar.offlinesynckit.internal.engine

import android.content.Context
import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncDiagnosticsSnapshot
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao

internal class DiagnosticsEngine(
    private val context: Context,
    private val dao: SyncQueueDao,
    private val config: SyncConfig
) {

    suspend fun getDiagnosticsSnapshot(
        isSyncPaused: Boolean
    ): SyncDiagnosticsSnapshot {
        val stats = dao.observeStatsSnapshot()

        return SyncDiagnosticsSnapshot(
            syncDirection = config.syncDirection,
            isSyncPaused = isSyncPaused,
            isPolicyAllowed = config.syncPolicy.canSync(context),
            autoSyncWhenOnline = config.autoSyncWhenOnline,
            enablePeriodicSync = config.enablePeriodicSync,
            periodicSyncIntervalMinutes = config.periodicSyncIntervalMinutes,
            syncBatchSize = config.syncBatchSize,
            maxRetryCount = config.retryPolicy.maxRetryCount,
            autoClearSyncedItems = config.autoClearSyncedItems,
            syncedItemRetentionMinutes = config.syncedItemRetentionMinutes,
            staleSyncingTimeoutMinutes = config.staleSyncingTimeoutMinutes,
            totalQueueItems = stats.totalCount,
            pendingCount = stats.pendingCount,
            syncingCount = stats.syncingCount,
            syncedCount = stats.syncedCount,
            failedCount = stats.failedCount,
            conflictCount = stats.conflictCount,
            giveUpCount = stats.giveUpCount
        )
    }
}