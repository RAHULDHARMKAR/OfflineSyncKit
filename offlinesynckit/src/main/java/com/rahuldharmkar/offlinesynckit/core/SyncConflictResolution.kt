package com.rahuldharmkar.offlinesynckit.core

sealed class SyncConflictResolution {

    data object KeepLocal : SyncConflictResolution()

    data object KeepServer : SyncConflictResolution()

    data object MarkManual : SyncConflictResolution()

    data class RetryWithPayload(
        val payload: String
    ) : SyncConflictResolution()
}