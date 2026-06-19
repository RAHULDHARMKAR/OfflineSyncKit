package com.rahuldharmkar.offlinesynckit.core

data class SyncRunResult(
    val totalProcessed: Int,
    val successCount: Int,
    val failedCount: Int,
    val conflictCount: Int,
    val giveUpCount: Int
) {
    companion object {
        fun empty(): SyncRunResult {
            return SyncRunResult(
                totalProcessed = 0,
                successCount = 0,
                failedCount = 0,
                conflictCount = 0,
                giveUpCount = 0
            )
        }
    }
}