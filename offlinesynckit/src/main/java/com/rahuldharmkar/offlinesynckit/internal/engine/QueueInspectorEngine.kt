package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.core.QueueInspectionReport
import com.rahuldharmkar.offlinesynckit.core.SyncQueueItem
import com.rahuldharmkar.offlinesynckit.core.SyncStatus
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao
import com.rahuldharmkar.offlinesynckit.internal.data.mapper.toDomain

internal class QueueInspectorEngine(
    private val dao: SyncQueueDao
) {

    suspend fun inspectQueue(): QueueInspectionReport {

        suspend fun load(status: SyncStatus): List<SyncQueueItem> {
            return dao.getItemsByStatus(status)
                .map { it.toDomain() }
        }
        return QueueInspectionReport(

            pendingItems = load(SyncStatus.PENDING),

            syncingItems = load(SyncStatus.SYNCING),

            failedItems = load(SyncStatus.FAILED),

            conflictItems = load(SyncStatus.CONFLICT),

            giveUpItems = load(SyncStatus.GIVE_UP),

            syncedItems = load(SyncStatus.SYNCED)

        )
    }
}