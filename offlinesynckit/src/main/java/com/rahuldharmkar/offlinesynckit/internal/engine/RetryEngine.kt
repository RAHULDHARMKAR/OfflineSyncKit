package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.core.SyncStatus
import com.rahuldharmkar.offlinesynckit.core.SyncRetryPolicy
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueEntity

internal class RetryEngine(
    private val dao: SyncQueueDao,
    private val retryPolicy: SyncRetryPolicy,
    private val log: (String) -> Unit
) {

    suspend fun markFailedOrGiveUp(
        item: SyncQueueEntity,
        error: String
    ): SyncStatus {
        val nextRetryCount = item.retryCount + 1

        val finalStatus = if (nextRetryCount >= retryPolicy.maxRetryCount) {
            SyncStatus.GIVE_UP
        } else {
            SyncStatus.FAILED
        }

        dao.markFailed(
            id = item.id,
            status = finalStatus,
            error = error
        )

        log("Sync failed queueId=${item.id} status=$finalStatus retryCount=$nextRetryCount error=$error")

        return finalStatus
    }
}