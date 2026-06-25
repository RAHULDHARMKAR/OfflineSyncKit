package com.rahuldharmkar.offlinesynckit.core

interface SyncInterceptor {

    suspend fun beforeSync(item: SyncQueueItem) = Unit

    suspend fun afterSync(
        item: SyncQueueItem,
        result: SyncApiResultSummary
    ) = Unit

    suspend fun onError(
        item: SyncQueueItem,
        error: String
    ) = Unit
}