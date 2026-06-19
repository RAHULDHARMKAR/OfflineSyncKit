package com.rahuldharmkar.offlinesynckit.core

data class SyncStats(
    val pendingCount: Int,
    val syncingCount: Int,
    val syncedCount: Int,
    val failedCount: Int,
    val conflictCount: Int,
    val giveUpCount: Int,
    val totalCount: Int
)