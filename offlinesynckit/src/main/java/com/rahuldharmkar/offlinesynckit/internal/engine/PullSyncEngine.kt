package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter
import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncPullRequest
import com.rahuldharmkar.offlinesynckit.core.SyncPullResult
import com.rahuldharmkar.offlinesynckit.core.SyncStatus
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueEntity
import com.rahuldharmkar.offlinesynckit.security.SyncSecurityManager

internal class PullSyncEngine(
    private val pullAdapter: SyncPullAdapter,
    private val dao: SyncQueueDao,
    private val syncStateManager: SyncStateManager,
    private val config: SyncConfig,
    private val log: (String) -> Unit,
    private val securityManager: SyncSecurityManager,

    ) {



    suspend fun pull(): SyncPullResult {

        val tenantId = config.tenantProvider?.getTenantId()



        val lastSyncToken = syncStateManager.getLastSyncToken(
            tenantId = tenantId
        )

        val request = SyncPullRequest(
            lastSyncToken = lastSyncToken,
            tenantId = tenantId,
            limit = config.syncBatchSize
        )

        log("Pull sync started. tenantId=${request.tenantId} limit=${request.limit}")

        val result = pullAdapter.pull(request)

        if (!result.success) {
            log("Pull sync failed. error=${result.errorMessage ?: "Unknown pull sync error"}")
            return result
        }

        // Persist server changes locally
        persistPulledItems(result)

        // Save the new sync token ONLY after persistence succeeds
        syncStateManager.saveLastSyncToken(
            tenantId = tenantId,
            token = result.nextSyncToken
        )

        log(
            "Pull sync success. items=${result.items.size} nextSyncToken=${result.nextSyncToken}"
        )

        return result
    }

    private suspend fun persistPulledItems(
        result: SyncPullResult
    ) {
        result.items.forEach { item ->
            dao.insert(
                SyncQueueEntity(
                    entityName = item.entityName,
                    entityId = item.entityId,
                    operation = item.operation,
                    payload = securityManager.encrypt(item.payload),
                    status = SyncStatus.SYNCED,
                    createdAt = item.updatedAt,
                    updatedAt = item.updatedAt
                )
            )
        }
    }
}