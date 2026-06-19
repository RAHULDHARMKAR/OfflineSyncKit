package com.rahuldharmkar.offlinesynckit

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncDatabase
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueEntity
import com.rahuldharmkar.offlinesynckit.internal.data.mapper.toDomain
import com.rahuldharmkar.offlinesynckit.internal.SyncValidator
import com.rahuldharmkar.offlinesynckit.internal.network.NetworkMonitor
import com.rahuldharmkar.offlinesynckit.internal.worker.OfflineSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OfflineSyncKit private constructor(
    private val context: Context,
    private val apiAdapter: com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter,
    private val pullAdapter: com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter?,
    private val config: com.rahuldharmkar.offlinesynckit.core.SyncConfig,
) {
    private val dao = SyncDatabase.getInstance(context).syncQueueDao()
    private val networkMonitor = NetworkMonitor(context)
    private val syncMutex = Mutex()

    @Volatile
    private var isSyncPaused: Boolean = false

    suspend fun enqueue(
        entityName: String,
        entityId: String,
        operation: com.rahuldharmkar.offlinesynckit.core.SyncOperation,
        payload: String
    ): Long {
        SyncValidator.validateEnqueueInput(
            entityName = entityName,
            entityId = entityId,
            payload = payload
        )

        applyMergePolicy(
            entityName = entityName,
            entityId = entityId,
            operation = operation
        )

        val queueId = dao.insert(
            SyncQueueEntity(
                entityName = entityName,
                entityId = entityId,
                operation = operation,
                payload = payload
            )
        )

        log("Enqueued item queueId=$queueId entityName=$entityName entityId=$entityId operation=$operation")

        config.eventListener?.onEvent(
            com.rahuldharmkar.offlinesynckit.core.SyncEvent.Enqueued(
                queueId = queueId,
                entityName = entityName,
                entityId = entityId,
                operation = operation
            )
        )

        return queueId
    }

    suspend fun enqueueAndSyncIfOnline(
        entityName: String,
        entityId: String,
        operation: com.rahuldharmkar.offlinesynckit.core.SyncOperation,
        payload: String
    ): Long {
        val queueId = enqueue(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            payload = payload
        )

        if (config.autoSyncWhenOnline && networkMonitor.isOnline()) {
            log("Network available. Scheduling auto sync.")
            scheduleAutoSync()
        } else {
            log("Auto sync not scheduled. autoSyncWhenOnline=${config.autoSyncWhenOnline}")
        }

        return queueId
    }

    fun observeQueue(): Flow<List<com.rahuldharmkar.offlinesynckit.core.SyncQueueItem>> {
        return dao.observeAll()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    suspend fun syncNow(limit: Int = config.syncBatchSize): com.rahuldharmkar.offlinesynckit.core.SyncRunResult {
        SyncValidator.validateSyncLimit(limit)

        if (isSyncPaused) {
            log("Sync skipped because sync is paused")
            return com.rahuldharmkar.offlinesynckit.core.SyncRunResult.empty()
        }

        if (syncMutex.isLocked) {
            log("Sync skipped because another sync is already running")
            return com.rahuldharmkar.offlinesynckit.core.SyncRunResult.empty()
        }

        return syncMutex.withLock {

            when (config.syncDirection) {
                com.rahuldharmkar.offlinesynckit.core.SyncDirection.PUSH -> {
                    log("Running PUSH sync")
                }

                com.rahuldharmkar.offlinesynckit.core.SyncDirection.PULL -> {
                    log("Running PULL sync")
                    runPullSync()
                    return@withLock com.rahuldharmkar.offlinesynckit.core.SyncRunResult.empty()
                }

                com.rahuldharmkar.offlinesynckit.core.SyncDirection.BOTH -> {
                    log("Running BOTH sync. Pull first, then push.")
                    runPullSync()
                }
            }


            resetStaleSyncingItems()



            val items = dao.getPendingItems(limit = limit)

            var successCount = 0
            var failedCount = 0
            var conflictCount = 0
            var giveUpCount = 0

            log("Sync started. itemCount=${items.size}")

            for (item in items) {
                try {
                    dao.updateStatus(item.id, com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCING)

                    log("Syncing queueId=${item.id} entityName=${item.entityName} entityId=${item.entityId}")

                    config.eventListener?.onEvent(
                        com.rahuldharmkar.offlinesynckit.core.SyncEvent.Started(
                            queueId = item.id,
                            entityName = item.entityName,
                            entityId = item.entityId
                        )
                    )

                    val result = apiAdapter.sync(
                        entityName = item.entityName,
                        entityId = item.entityId,
                        operation = item.operation,
                        payload = item.payload
                    )

                    when {
                        result.success -> {
                            dao.updateStatus(item.id, com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED)
                            successCount++

                            log("Sync success queueId=${item.id}")

                            config.eventListener?.onEvent(
                                com.rahuldharmkar.offlinesynckit.core.SyncEvent.Success(
                                    queueId = item.id,
                                    entityName = item.entityName,
                                    entityId = item.entityId
                                )
                            )
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
                        }

                        else -> {
                            val status = markFailedOrGiveUp(
                                item = item,
                                error = result.errorMessage ?: "Unknown sync error"
                            )

                            if (status == com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP) {
                                giveUpCount++
                            } else {
                                failedCount++
                            }
                        }
                    }
                } catch (e: Exception) {
                    val status = markFailedOrGiveUp(
                        item = item,
                        error = e.message ?: "Unexpected sync error"
                    )

                    if (status == com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP) {
                        giveUpCount++
                    } else {
                        failedCount++
                    }
                }
            }

            if (config.autoClearSyncedItems) {
                clearOldSyncedItems()
            }

            log("Sync completed.")

            com.rahuldharmkar.offlinesynckit.core.SyncRunResult(
                totalProcessed = items.size,
                successCount = successCount,
                failedCount = failedCount,
                conflictCount = conflictCount,
                giveUpCount = giveUpCount
            )
        }
    }
    fun scheduleAutoSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                AUTO_SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )

        log("One-time auto sync scheduled")
    }

    suspend fun retryItem(id: Long) {
        dao.resetForRetry(id = id)
        log("Retry scheduled for queueId=$id")
        scheduleAutoSync()
    }

    suspend fun retryAllGiveUpItems() {
        dao.resetAllByStatus(
            oldStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP,
            newStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING
        )

        log("All GIVE_UP items reset for retry")
        scheduleAutoSync()
    }

    suspend fun getGiveUpItems(): List<com.rahuldharmkar.offlinesynckit.core.SyncQueueItem> {
        return dao.getItemsByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP)
            .map { it.toDomain() }
    }

    suspend fun clearSynced() {
        dao.deleteByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED)
        log("Cleared synced items")
    }

    fun enablePeriodicAutoSync(
        repeatIntervalMinutes: Long = DEFAULT_PERIODIC_SYNC_INTERVAL_MINUTES
    ) {
        require(repeatIntervalMinutes >= 15L) {
            "WorkManager minimum periodic interval is 15 minutes."
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<OfflineSyncWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

        log("Periodic auto sync enabled intervalMinutes=$repeatIntervalMinutes")
    }

    fun disablePeriodicAutoSync() {
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)

        log("Periodic auto sync disabled")
    }

    private suspend fun markFailedOrGiveUp(
        item: SyncQueueEntity,
        error: String
    ): com.rahuldharmkar.offlinesynckit.core.SyncStatus {
        val nextRetryCount = item.retryCount + 1

        val finalStatus = if (nextRetryCount >= config.retryPolicy.maxRetryCount) {
            com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP
        } else {
            com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED
        }

        dao.markFailed(
            id = item.id,
            status = finalStatus,
            error = error
        )

        log("Sync failed queueId=${item.id} status=$finalStatus retryCount=$nextRetryCount error=$error")

        val event = if (finalStatus == com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP) {
            com.rahuldharmkar.offlinesynckit.core.SyncEvent.GiveUp(
                queueId = item.id,
                entityName = item.entityName,
                entityId = item.entityId,
                error = error
            )
        } else {
            com.rahuldharmkar.offlinesynckit.core.SyncEvent.Failed(
                queueId = item.id,
                entityName = item.entityName,
                entityId = item.entityId,
                error = error
            )
        }

        config.eventListener?.onEvent(event)

        return finalStatus
    }

    private suspend fun applyMergePolicy(
        entityName: String,
        entityId: String,
        operation: com.rahuldharmkar.offlinesynckit.core.SyncOperation
    ) {
        when (config.mergePolicy) {
            com.rahuldharmkar.offlinesynckit.core.SyncMergePolicy.APPEND_ONLY -> Unit

            com.rahuldharmkar.offlinesynckit.core.SyncMergePolicy.REPLACE_SAME_ENTITY_OPERATION -> {
                dao.deletePendingSameEntityOperation(
                    entityName = entityName,
                    entityId = entityId,
                    operation = operation
                )

                log("Applied merge policy REPLACE_SAME_ENTITY_OPERATION entityName=$entityName entityId=$entityId operation=$operation")
            }

            com.rahuldharmkar.offlinesynckit.core.SyncMergePolicy.REPLACE_SAME_ENTITY -> {
                dao.deletePendingSameEntity(
                    entityName = entityName,
                    entityId = entityId
                )

                log("Applied merge policy REPLACE_SAME_ENTITY entityName=$entityName entityId=$entityId")
            }
        }
    }

    private fun log(message: String) {
        config.logger?.log(message)
    }

    companion object {

        private const val AUTO_SYNC_WORK_NAME = "offline_sync_work"
        private const val PERIODIC_SYNC_WORK_NAME = "offline_periodic_sync_work"
        private const val DEFAULT_PERIODIC_SYNC_INTERVAL_MINUTES = 15L

        fun create(
            context: Context,
            apiAdapter: com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter,
            pullAdapter: com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter? = null,
            config: com.rahuldharmkar.offlinesynckit.core.SyncConfig = com.rahuldharmkar.offlinesynckit.core.SyncConfig()
        ): OfflineSyncKit {
            OfflineSyncConfig.initialize(
                adapter = apiAdapter,
                pullAdapter = pullAdapter,
                config = config
            )

            val syncKit = OfflineSyncKit(
                context = context.applicationContext,
                apiAdapter = apiAdapter,
                pullAdapter = pullAdapter,
                config = config
            )

            if (config.enablePeriodicSync) {
                syncKit.enablePeriodicAutoSync(
                    repeatIntervalMinutes = config.periodicSyncIntervalMinutes
                )
            }

            return syncKit
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

    suspend fun deleteItem(id: Long) {
        dao.deleteById(id)
        log("Deleted queue item id=$id")
    }

    suspend fun clearAllItems() {
        dao.clearAll()
        log("Cleared all queue items")
    }

    private suspend fun clearOldSyncedItems() {
        val olderThan = System.currentTimeMillis() -
                TimeUnit.MINUTES.toMillis(config.syncedItemRetentionMinutes)

        dao.deleteSyncedOlderThan(
            olderThan = olderThan
        )

        log("Old synced items cleared. retentionMinutes=${config.syncedItemRetentionMinutes}")
    }

    fun pauseSync() {
        isSyncPaused = true
        log("Sync paused")
    }

    fun resumeSync() {
        isSyncPaused = false
        log("Sync resumed")

        if (!isSyncPaused && config.autoSyncWhenOnline && networkMonitor.isOnline()) {
            scheduleAutoSync()
        }
    }

    fun isSyncPaused(): Boolean {
        return isSyncPaused
    }

    suspend fun getStats(): com.rahuldharmkar.offlinesynckit.core.SyncStats {
        return com.rahuldharmkar.offlinesynckit.core.SyncStats(
            pendingCount = dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING),
            syncingCount = dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCING),
            syncedCount = dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED),
            failedCount = dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED),
            conflictCount = dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.CONFLICT),
            giveUpCount = dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP),
            totalCount = dao.countAll()
        )
    }

    fun observeStats(): Flow<com.rahuldharmkar.offlinesynckit.core.SyncStats> {
        return dao.observeForStats()
            .map { items ->
                com.rahuldharmkar.offlinesynckit.core.SyncStats(
                    pendingCount = items.count { it.status == com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING },
                    syncingCount = items.count { it.status == com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCING },
                    syncedCount = items.count { it.status == com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED },
                    failedCount = items.count { it.status == com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED },
                    conflictCount = items.count { it.status == com.rahuldharmkar.offlinesynckit.core.SyncStatus.CONFLICT },
                    giveUpCount = items.count { it.status == com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP },
                    totalCount = items.size
                )
            }
    }

    private suspend fun runPullSync(): Boolean {
        val adapter = pullAdapter

        if (adapter == null) {
            log("Pull sync skipped because SyncPullAdapter is not provided")
            return false
        }

        val result = adapter.pull()

        return if (result.success) {
            log("Pull sync success. pulledCount=${result.pulledCount}")
            true
        } else {
            log("Pull sync failed. error=${result.errorMessage}")
            false
        }
    }

    suspend fun <T> enqueueObject(
        entityName: String,
        entityId: String,
        operation: com.rahuldharmkar.offlinesynckit.core.SyncOperation,
        entity: T,
        serializer: (T) -> String
    ): Long {
        val payload = serializer(entity)

        return enqueue(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            payload = payload
        )
    }


    suspend fun <T> enqueueObjectAndSyncIfOnline(
        entityName: String,
        entityId: String,
        operation: com.rahuldharmkar.offlinesynckit.core.SyncOperation,
        entity: T,
        serializer: (T) -> String
    ): Long {
        val payload = serializer(entity)

        return enqueueAndSyncIfOnline(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            payload = payload
        )
    }

    private suspend fun handleConflict(
        item: SyncQueueEntity,
        serverPayload: String?
    ): Boolean {
        log("Sync conflict queueId=${item.id}")

        val conflict = com.rahuldharmkar.offlinesynckit.core.SyncConflict(
            queueId = item.id,
            entityName = item.entityName,
            entityId = item.entityId,
            operation = item.operation,
            localPayload = item.payload,
            serverPayload = serverPayload
        )

        val resolution = config.conflictResolver?.resolve(conflict)
            ?: when (config.conflictStrategy) {
                com.rahuldharmkar.offlinesynckit.core.SyncConflictStrategy.LOCAL_WINS -> com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution.KeepLocal
                com.rahuldharmkar.offlinesynckit.core.SyncConflictStrategy.SERVER_WINS -> com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution.KeepServer
                com.rahuldharmkar.offlinesynckit.core.SyncConflictStrategy.MANUAL -> com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution.MarkManual
            }

        return when (resolution) {
            com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution.KeepLocal -> {
                dao.updateStatus(item.id, com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED)

                config.eventListener?.onEvent(
                    com.rahuldharmkar.offlinesynckit.core.SyncEvent.Failed(
                        queueId = item.id,
                        entityName = item.entityName,
                        entityId = item.entityId,
                        error = "Conflict resolved with LOCAL_WINS. Item will retry."
                    )
                )

                log("Conflict resolved LOCAL_WINS queueId=${item.id}")
                true
            }

            com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution.KeepServer -> {
                dao.updateStatus(item.id, com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED)

                config.eventListener?.onEvent(
                    com.rahuldharmkar.offlinesynckit.core.SyncEvent.Success(
                        queueId = item.id,
                        entityName = item.entityName,
                        entityId = item.entityId
                    )
                )

                log("Conflict resolved SERVER_WINS queueId=${item.id}")
                true
            }

            com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution.MarkManual -> {
                dao.updateStatus(item.id, com.rahuldharmkar.offlinesynckit.core.SyncStatus.CONFLICT)

                config.eventListener?.onEvent(
                    com.rahuldharmkar.offlinesynckit.core.SyncEvent.Conflict(
                        queueId = item.id,
                        entityName = item.entityName,
                        entityId = item.entityId
                    )
                )

                log("Conflict marked MANUAL queueId=${item.id}")
                false
            }

            is com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution.RetryWithPayload -> {
                dao.updatePayloadAndStatus(
                    id = item.id,
                    payload = resolution.payload,
                    status = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING
                )

                config.eventListener?.onEvent(
                    com.rahuldharmkar.offlinesynckit.core.SyncEvent.Failed(
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

}