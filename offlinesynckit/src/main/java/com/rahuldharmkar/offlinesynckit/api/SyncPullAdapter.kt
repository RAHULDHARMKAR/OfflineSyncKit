package com.rahuldharmkar.offlinesynckit.api

interface SyncPullAdapter {

    suspend fun pull(): SyncPullResult
}

data class SyncPullResult(
    val success: Boolean,
    val pulledCount: Int = 0,
    val errorMessage: String? = null
)