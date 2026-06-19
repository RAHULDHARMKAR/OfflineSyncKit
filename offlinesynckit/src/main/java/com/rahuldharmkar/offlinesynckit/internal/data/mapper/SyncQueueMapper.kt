package com.rahuldharmkar.offlinesynckit.internal.data.mapper

import com.rahuldharmkar.offlinesynckit.core.SyncQueueItem
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueEntity

internal fun SyncQueueEntity.toDomain(): com.rahuldharmkar.offlinesynckit.core.SyncQueueItem {
    return com.rahuldharmkar.offlinesynckit.core.SyncQueueItem(
        id = id,
        entityName = entityName,
        entityId = entityId,
        operation = operation,
        status = status,
        retryCount = retryCount,
        lastError = lastError,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}