package com.rahuldharmkar.offlinesynckit.internal.engine

import android.content.Context
import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncHealthReport
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao

internal class HealthReportEngine(
    private val context: Context,
    private val dao: SyncQueueDao,
    private val config: SyncConfig
) {

    suspend fun getHealthReport(
        isSyncPaused: Boolean
    ): SyncHealthReport {
        val stats = dao.observeStatsSnapshot()

        return SyncHealthReport(
            totalQueueItems = stats.totalCount,
            pendingCount = stats.pendingCount,
            syncingCount = stats.syncingCount,
            syncedCount = stats.syncedCount,
            failedCount = stats.failedCount,
            conflictCount = stats.conflictCount,
            giveUpCount = stats.giveUpCount,
            isSyncPaused = isSyncPaused,
            syncDirection = config.syncDirection,
            isPolicyAllowed = config.syncPolicy.canSync(context)
        )
    }
}