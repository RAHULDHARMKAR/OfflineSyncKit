package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter
import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncPullRequest
import com.rahuldharmkar.offlinesynckit.core.SyncPullResult

internal class PullSyncEngine(
    private val pullAdapter: SyncPullAdapter,
    private val config: SyncConfig,
    private val log: (String) -> Unit
) {

    suspend fun pull(): SyncPullResult {
        val request = SyncPullRequest(
            tenantId = config.tenantProvider?.getTenantId(),
            limit = config.syncBatchSize
        )

        log("Pull sync started. tenantId=${request.tenantId} limit=${request.limit}")

        val result = pullAdapter.pull(request)

        if (result.success) {
            log(
                "Pull sync success. items=${result.items.size} nextSyncToken=${result.nextSyncToken}"
            )
        } else {
            log(
                "Pull sync failed. error=${result.errorMessage ?: "Unknown pull sync error"}"
            )
        }

        return result
    }
}