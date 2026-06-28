package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.core.SyncQueueFilter
import com.rahuldharmkar.offlinesynckit.core.SyncQueueItem
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao
import com.rahuldharmkar.offlinesynckit.internal.data.mapper.toDomain

internal class QueueQueryEngine(
    private val dao: SyncQueueDao
) {

    suspend fun queryQueue(
        filter: SyncQueueFilter
    ): List<SyncQueueItem> {
        return dao.queryQueue(
            status = filter.status,
            entityName = filter.entityName,
            operation = filter.operation,
            tenantId = filter.tenantId,
            limit = filter.limit
        ).map { entity ->
            entity.toDomain()
        }
    }
}