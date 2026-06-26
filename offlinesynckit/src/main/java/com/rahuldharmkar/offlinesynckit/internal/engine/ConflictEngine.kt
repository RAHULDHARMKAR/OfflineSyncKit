package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.core.SyncConflict
import com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution
import com.rahuldharmkar.offlinesynckit.core.SyncConflictStrategy
import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncEvent
import com.rahuldharmkar.offlinesynckit.core.SyncStatus
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueEntity

internal class ConflictEngine(
    private val dao: SyncQueueDao,
    private val config: SyncConfig,
    private val log: (String) -> Unit
) {

    suspend fun handleConflict(
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
}