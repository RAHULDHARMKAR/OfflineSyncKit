package com.rahuldharmkar.offlinesynckit.api

import com.rahuldharmkar.offlinesynckit.core.SyncOperation

interface SyncApiAdapter {

    suspend fun sync(
        entityName: String,
        entityId: String,
        operation: SyncOperation,
        payload: String
    ): SyncApiResult
}

data class SyncApiResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val isConflict: Boolean = false,
    val serverPayload: String? = null
)