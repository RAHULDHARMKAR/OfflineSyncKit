package com.rahuldharmkar.offlinesynckit.api

import com.rahuldharmkar.offlinesynckit.core.SyncRequest

fun interface SyncApiAdapter {

    suspend fun sync(
        request: SyncRequest
    ): SyncApiResult
}

data class SyncApiResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val isConflict: Boolean = false,
    val serverPayload: String? = null
)