package com.rahuldharmkar.offlinesynckit.sampleApp

import com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncPullRequest
import com.rahuldharmkar.offlinesynckit.core.SyncPullResult
import com.rahuldharmkar.offlinesynckit.core.SyncPulledItem

class FakeCustomerPullAdapter : SyncPullAdapter {

    override suspend fun pull(
        request: SyncPullRequest
    ): SyncPullResult {

        return SyncPullResult(
            success = true,
            nextSyncToken = System.currentTimeMillis().toString(),
            items = listOf(
                SyncPulledItem(
                    entityName = "customer",
                    entityId = "server_customer_001",
                    operation = SyncOperation.CREATE,
                    payload = """
                        {
                            "id":"server_customer_001",
                            "name":"John Doe",
                            "phone":"9999999999"
                        }
                    """.trimIndent(),
                    updatedAt = System.currentTimeMillis(),
                    tenantId = request.tenantId
                )
            )
        )
    }
}