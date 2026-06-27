package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncApiResult
import com.rahuldharmkar.offlinesynckit.core.SyncApiResultSummary
import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncEvent
import com.rahuldharmkar.offlinesynckit.core.SyncRequest
import com.rahuldharmkar.offlinesynckit.core.SyncRunResult
import com.rahuldharmkar.offlinesynckit.core.SyncStatus
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueEntity
import com.rahuldharmkar.offlinesynckit.internal.data.mapper.toDomain
import com.rahuldharmkar.offlinesynckit.security.SyncSecurityManager
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
                        val resolved = conflictEngine.handleConflict(
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
                        val status = retryEngine.markFailedOrGiveUp(
                            item = item,
                            error = result.errorMessage ?: "Unknown sync error"
                        )

                        dispatchFailureEvent(
                            item = item,
                            error = result.errorMessage ?: "Unknown sync error",
                            status = status
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
                val status = retryEngine.markFailedOrGiveUp(
                    item = item,
                    error = e.message ?: "Unexpected sync error"
                )

                dispatchFailureEvent(
                    item = item,
                    error = e.message ?: "Unexpected sync error",
                    status = status
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
        val decryptedPayload = securityManager.decrypt(item.payload)

        val finalHeaders =
            securityManager.createHeaders(
                decryptedPayload,
                headers
            )

        return apiAdapter.sync(
            SyncRequest(
                entityName = item.entityName,
                entityId = item.entityId,
                operation = item.operation,
                payload = decryptedPayload,
                authToken = authToken,
                headers = finalHeaders,
                retryCount = item.retryCount,
                queuedAt = item.createdAt,
                updatedAt = item.updatedAt,
                metadata = emptyMap()
            )
        )
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

    private val retryEngine = RetryEngine(
        dao = dao,
        retryPolicy = config.retryPolicy,
        log = log
    )

    private fun dispatchFailureEvent(
        item: SyncQueueEntity,
        error: String,
        status: SyncStatus
    ) {
        val event = if (status == SyncStatus.GIVE_UP) {
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
    }

    private val conflictEngine = ConflictEngine(
        dao = dao,
        config = config,
        log = log
    )
    private val securityManager = SyncSecurityManager(config)


}