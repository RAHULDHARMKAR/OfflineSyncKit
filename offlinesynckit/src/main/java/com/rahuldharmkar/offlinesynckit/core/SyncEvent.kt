package com.rahuldharmkar.offlinesynckit.core

sealed class SyncEvent {

    data class Enqueued(
        val queueId: Long,
        val entityName: String,
        val entityId: String,
        val operation: SyncOperation
    ) : SyncEvent()

    data class Started(
        val queueId: Long,
        val entityName: String,
        val entityId: String
    ) : SyncEvent()

    data class Success(
        val queueId: Long,
        val entityName: String,
        val entityId: String
    ) : SyncEvent()

    data class Failed(
        val queueId: Long,
        val entityName: String,
        val entityId: String,
        val error: String
    ) : SyncEvent()

    data class Conflict(
        val queueId: Long,
        val entityName: String,
        val entityId: String
    ) : SyncEvent()

    data class GiveUp(
        val queueId: Long,
        val entityName: String,
        val entityId: String,
        val error: String
    ) : SyncEvent()
}