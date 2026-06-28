package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncStateDao
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncStateEntity

internal class SyncStateManager(
    private val dao: SyncStateDao
) {

    suspend fun getLastSyncToken(
        tenantId: String?
    ): String? {
        return dao.getState(keyForTenant(tenantId))
            ?.lastSyncToken
    }

    suspend fun saveLastSyncToken(
        tenantId: String?,
        token: String?
    ) {
        if (token == null) return

        dao.upsert(
            SyncStateEntity(
                key = keyForTenant(tenantId),
                lastSyncToken = token,
                lastPulledAt = System.currentTimeMillis()
            )
        )
    }

    private fun keyForTenant(
        tenantId: String?
    ): String {
        return tenantId ?: DEFAULT_TENANT_KEY
    }

    private companion object {
        private const val DEFAULT_TENANT_KEY = "default"
    }
}