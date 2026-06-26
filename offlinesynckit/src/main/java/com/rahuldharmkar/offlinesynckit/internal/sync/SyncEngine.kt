package com.rahuldharmkar.offlinesynckit.internal.sync

import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncApiResult
import com.rahuldharmkar.offlinesynckit.core.SyncApiResultSummary
import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncConflict
import com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution
import com.rahuldharmkar.offlinesynckit.core.SyncConflictStrategy
import com.rahuldharmkar.offlinesynckit.core.SyncEvent
import com.rahuldharmkar.offlinesynckit.core.SyncRequest
import com.rahuldharmkar.offlinesynckit.core.SyncRunResult
import com.rahuldharmkar.offlinesynckit.core.SyncStatus
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueEntity
import com.rahuldharmkar.offlinesynckit.internal.data.mapper.toDomain
import java.util.concurrent.TimeUnit

internal class SyncEngine(
    private val dao: SyncQueueDao,
    private val apiAdapter: SyncApiAdapter,
    private val config: SyncConfig,
    private val log: (String) -> Unit
) {

    suspend fun syncNow(limit: Int): SyncRunResult {
        resetStaleSyncingItems()

        val items = dao.getPendingItems(limit = limit)

        var successCount = 0
        var failedCount = 0
        var conflictCount = 0
        var giveUpCount = 0

        log("Sync started. itemCount=${items.size}")

        for (item in items) {
            val queueItem = item.toDomain()

            try {
                dao.updateStatus(item.id, SyncStatus.SYNCING)

                log("Syncing queueId=${item.id} entityName=${item.entityName} entityId=${item.entityId}")

                config.eventListener?.onEvent(
                    SyncEvent.Started(
                        queueId = item.id,
                        entityName = item.entityName,
                        entityId = item.entityId
                    )
                )

                config.interceptors.forEach { interceptor ->
                    interceptor.beforeSync(queueItem)
                }

                val result = executeSync(item)

                when {
                    result.success -> {
                        dao.updateStatus(item.id, SyncStatus.SYNCED)
                        successCount++

                        log("Sync success queueId=${item.id}")

                        config.eventListener?.onEvent(
                            SyncEvent.Success(
                                queueId = item.id,
                                entityName = item.entityName,
                                entityId = item.entityId
                            )
                        )

                        config.interceptors.forEach { interceptor ->
                            interceptor.afterSync(
                                item = queueItem,
                                result = SyncApiResultSummary(
                                    success = true,
                                    conflict = false,
                                    finalStatus = SyncStatus.SYNCED
                                )
                            )
                        }
                    }

                    result.isConflict -> {
                        val resolved = handleConflict(
                            item = item,
                            serverPayload = result.serverPayload
                        )

                        if (resolved) {
                            failedCount++
                        } else {
                            conflictCount++
                        }

                        config.interceptors.forEach { interceptor ->
                            interceptor.afterSync(
                                item = queueItem,
                                result = SyncApiResultSummary(
                                    success = false,
                                    conflict = true,
                                    finalStatus = SyncStatus.CONFLICT
                                )
                            )
                        }
                    }

                    else -> {
                        val status = markFailedOrGiveUp(
                            item = item,
                            error = result.errorMessage ?: "Unknown sync error"
                        )

                        if (status == SyncStatus.GIVE_UP) {
                            giveUpCount++
                        } else {
                            failedCount++
                        }

                        config.interceptors.forEach { interceptor ->
                            interceptor.onError(
                                item = queueItem,
                                error = result.errorMessage ?: "Unknown sync error"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                val status = markFailedOrGiveUp(
                    item = item,
                    error = e.message ?: "Unexpected sync error"
                )

                if (status == SyncStatus.GIVE_UP) {
                    giveUpCount++
                } else {
                    failedCount++
                }

                config.interceptors.forEach { interceptor ->
                    interceptor.onError(
                        item = queueItem,
                        error = e.message ?: "Unexpected sync error"
                    )
                }
            }
        }

        if (config.autoClearSyncedItems) {
            clearOldSyncedItems()
        }

        log("Sync completed.")

        return SyncRunResult(
            totalProcessed = items.size,
            successCount = successCount,
            failedCount = failedCount,
            conflictCount = conflictCount,
            giveUpCount = giveUpCount
        )
    }

    private suspend fun executeSync(
        item: SyncQueueEntity
    ): SyncApiResult {
        val authToken = config.authTokenProvider?.getToken()
        val headers = config.headerProvider?.getHeaders().orEmpty()

        return apiAdapter.sync(
            SyncRequest(
                entityName = item.entityName,
                entityId = item.entityId,
                operation = item.operation,
                payload = item.payload,
                authToken = authToken,
                headers = headers,
                retryCount = item.retryCount,
                queuedAt = item.createdAt,
                updatedAt = item.updatedAt,
                metadata = emptyMap()
            )
        )
    }

    private suspend fun markFailedOrGiveUp(
        item: SyncQueueEntity,
        error: String
    ): SyncStatus {
        val nextRetryCount = item.retryCount + 1

        val finalStatus = if (nextRetryCount >= config.retryPolicy.maxRetryCount) {
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

        val event = if (finalStatus == SyncStatus.GIVE_UP) {
            SyncEvent.GiveUp(
                queueId = item.id,
                entityName = item.entityName,
                entityId = item.entityId,
                error = error
            )
        } else {
            SyncEvent.Failed(
                queueId = item.id,
                entityName = item.entityName,
                entityId = item.entityId,
                error = error
            )
        }

        config.eventListener?.onEvent(event)

        return finalStatus
    }

    private suspend fun handleConflict(
        item: SyncQueueEntity,
        serverPayload: String?
    ): Boolean {
        log("Sync conflict queueId=${item.id}")

        val conflict = SyncConflict(
            queueId = item.id,
            entityName = item.entityName,
            entityId = item.entityId,
            operation = item.operation,
            localPayload = item.payload,
            serverPayload = serverPayload
        )

        val resolution = config.conflictResolver?.resolve(conflict)
            ?: when (config.conflictStrategy) {
                SyncConflictStrategy.LOCAL_WINS -> SyncConflictResolution.KeepLocal
                SyncConflictStrategy.SERVER_WINS -> SyncConflictResolution.KeepServer
                SyncConflictStrategy.MANUAL -> SyncConflictResolution.MarkManual
            }

        return when (resolution) {
            SyncConflictResolution.KeepLocal -> {
                dao.updateStatus(item.id, SyncStatus.FAILED)

                config.eventListener?.onEvent(
                    SyncEvent.Failed(
                        queueId = item.id,
                        entityName = item.entityName,
                        entityId = item.entityId,
                        error = "Conflict resolved with LOCAL_WINS. Item will retry."
                    )
                )

                log("Conflict resolved LOCAL_WINS queueId=${item.id}")
                true
            }

            SyncConflictResolution.KeepServer -> {
                dao.updateStatus(item.id, SyncStatus.SYNCED)

                config.eventListener?.onEvent(
                    SyncEvent.Success(
                        queueId = item.id,
                        entityName = item.entityName,
                        entityId = item.entityId
                    )
                )

                log("Conflict resolved SERVER_WINS queueId=${item.id}")
                true
            }

            SyncConflictResolution.MarkManual -> {
                dao.updateStatus(item.id, SyncStatus.CONFLICT)

                config.eventListener?.onEvent(
                    SyncEvent.Conflict(
                        queueId = item.id,
                        entityName = item.entityName,
                        entityId = item.entityId
                    )
                )

                log("Conflict marked MANUAL queueId=${item.id}")
                false
            }

            is SyncConflictResolution.RetryWithPayload -> {
                dao.updatePayloadAndStatus(
                    id = item.id,
                    payload = resolution.payload,
                    status = SyncStatus.PENDING
                )

                config.eventListener?.onEvent(
                    SyncEvent.Failed(
                        queueId = item.id,
                        entityName = item.entityName,
                        entityId = item.entityId,
                        error = "Conflict resolved with merged payload. Item will retry."
                    )
                )

                log("Conflict resolved with merged payload queueId=${item.id}")
                true
            }
        }
    }

    private suspend fun resetStaleSyncingItems() {
        val staleBefore = System.currentTimeMillis() -
                TimeUnit.MINUTES.toMillis(config.staleSyncingTimeoutMinutes)

        dao.resetStaleSyncingItems(
            staleBefore = staleBefore
        )

        log("Stale SYNCING items reset if older than ${config.staleSyncingTimeoutMinutes} minutes")
    }

    private suspend fun clearOldSyncedItems() {
        val olderThan = System.currentTimeMillis() -
                TimeUnit.MINUTES.toMillis(config.syncedItemRetentionMinutes)

        dao.deleteSyncedOlderThan(
            olderThan = olderThan
        )

        log("Old synced items cleared. retentionMinutes=${config.syncedItemRetentionMinutes}")
    }
}