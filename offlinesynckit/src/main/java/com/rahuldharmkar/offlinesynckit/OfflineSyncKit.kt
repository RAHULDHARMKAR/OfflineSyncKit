package com.rahuldharmkar.offlinesynckit

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter
import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncDirection
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncQueueItem
import com.rahuldharmkar.offlinesynckit.core.SyncRunResult
import com.rahuldharmkar.offlinesynckit.core.SyncSerializer
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
import kotlin.reflect.KClass
import com.rahuldharmkar.offlinesynckit.internal.engine.SyncEngine

class OfflineSyncKit private constructor(
    private val context: Context,
    private val apiAdapter: SyncApiAdapter,
    private val pullAdapter: SyncPullAdapter?,
    private val config: SyncConfig,
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

    suspend fun syncNow(limit: Int = config.syncBatchSize): SyncRunResult {
        SyncValidator.validateSyncLimit(limit)

        if (isSyncPaused) {
            log("Sync skipped because sync is paused")
            return SyncRunResult.empty()
        }

        if (syncMutex.isLocked) {
            log("Sync skipped because another sync is already running")
            return SyncRunResult.empty()
        }

        return syncMutex.withLock {
            when (config.syncDirection) {
                SyncDirection.PUSH -> {
                    log("Running PUSH sync")
                }

                SyncDirection.PULL -> {
                    log("Running PULL sync")
                    runPullSync()
                    return@withLock SyncRunResult.empty()
                }

                SyncDirection.BOTH -> {
                    log("Running BOTH sync. Pull first, then push.")
                    runPullSync()
                }
            }

            syncEngine.syncNow(limit)
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
            apiAdapter: SyncApiAdapter,
            pullAdapter: SyncPullAdapter? = null,
            config: SyncConfig = SyncConfig()
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



    suspend fun deleteItem(id: Long) {
        dao.deleteById(id)
        log("Deleted queue item id=$id")
    }

    suspend fun clearAllItems() {
        dao.clearAll()
        log("Cleared all queue items")
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

    /**
     * Preferred serializer API.
     */

    suspend fun <T> enqueueObject(
        entityName: String,
        entityId: String,
        operation: SyncOperation,
        entity: T,
        serializer: SyncSerializer<T>
    ): Long {
        val payload = serializer.serialize(entity)

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
        operation: SyncOperation,
        entity: T,
        serializer: SyncSerializer<T>
    ): Long {
        val payload = serializer.serialize(entity)

        return enqueueAndSyncIfOnline(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            payload = payload
        )
    }



    /**
     * Legacy serializer API.
     * Kept for backward compatibility.
     */

    suspend fun <T> enqueueObject(
        entityName: String,
        entityId: String,
        operation: SyncOperation,
        entity: T,
        serializer: (T) -> String
    ): Long {
        return enqueueObject(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            entity = entity,
            serializer = SyncSerializer(serializer)
        )
    }


    suspend fun <T> enqueueObjectAndSyncIfOnline(
        entityName: String,
        entityId: String,
        operation: SyncOperation,
        entity: T,
        serializer: (T) -> String
    ): Long {
        return enqueueObjectAndSyncIfOnline(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            entity = entity,
            serializer = SyncSerializer(serializer)
        )
    }



    suspend fun <T : Any> enqueueObject(
        entityName: String,
        entityId: String,
        operation: SyncOperation,
        entity: T,
        type: KClass<T>
    ): Long {
        val serializer = config.serializerRegistry.get(type)
            ?: error("No SyncSerializer registered for ${type.simpleName}")

        return enqueueObject(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            entity = entity,
            serializer = serializer
        )
    }

    suspend  fun < T : Any> enqueueObjectAndSyncIfOnline(
        entityName: String,
        entityId: String,
        operation: SyncOperation,
        entity: T,
        type: KClass<T>
    ): Long {
        val serializer = config.serializerRegistry.get(type)
            ?: error("No SyncSerializer registered for ${type.simpleName}")

        return enqueueObjectAndSyncIfOnline(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            entity = entity,
            serializer = serializer
        )
    }

    private fun SyncQueueEntity.toQueueItem(): SyncQueueItem {
        return this.toDomain()
    }



    private val syncEngine = SyncEngine(
        dao = dao,
        apiAdapter = apiAdapter,
        config = config,
        log = ::log
    )


    suspend fun <T : Any> enqueueObjects(
        entityName: String,
        items: List<T>,
        operation: SyncOperation,
        type: KClass<T>,
        entityIdProvider: (T) -> String
    ): List<Long> {
        require(items.isNotEmpty()) {
            "items must not be empty"
        }

        val serializer = config.serializerRegistry.get(type)
            ?: error("No SyncSerializer registered for ${type.simpleName}")

        return items.map { item ->
            enqueueObject(
                entityName = entityName,
                entityId = entityIdProvider(item),
                operation = operation,
                entity = item,
                serializer = serializer
            )
        }
    }

    suspend fun <T : Any> enqueueObjectsAndSyncIfOnline(
        entityName: String,
        items: List<T>,
        operation: SyncOperation,
        type: KClass<T>,
        entityIdProvider: (T) -> String
    ): List<Long> {
        val ids = enqueueObjects(
            entityName = entityName,
            items = items,
            operation = operation,
            type = type,
            entityIdProvider = entityIdProvider
        )

        if (!isSyncPaused && config.autoSyncWhenOnline && networkMonitor.isOnline()) {
            scheduleAutoSync()
        }

        return ids
    }

    suspend fun <T : Any> enqueueObjects(
        entityName: String,
        items: List<T>,
        operation: SyncOperation,
        serializer: (T) -> String,
        entityIdProvider: (T) -> String
    ): List<Long> {
        require(items.isNotEmpty()) {
            "items must not be empty"
        }

        return items.map { item ->
            enqueueObject(
                entityName = entityName,
                entityId = entityIdProvider(item),
                operation = operation,
                entity = item,
                serializer = serializer
            )
        }
    }

    suspend fun <T : Any> enqueueObjectsAndSyncIfOnline(
        entityName: String,
        items: List<T>,
        operation: SyncOperation,
        serializer: (T) -> String,
        entityIdProvider: (T) -> String
    ): List<Long> {
        val ids = enqueueObjects(
            entityName = entityName,
            items = items,
            operation = operation,
            serializer = serializer,
            entityIdProvider = entityIdProvider
        )

        if (!isSyncPaused && config.autoSyncWhenOnline && networkMonitor.isOnline()) {
            scheduleAutoSync()
        }

        return ids
    }

    suspend fun observeQueueSnapshot(): List<SyncQueueItem> {
        return dao.getAllItems()
            .map { it.toDomain() }
    }

}