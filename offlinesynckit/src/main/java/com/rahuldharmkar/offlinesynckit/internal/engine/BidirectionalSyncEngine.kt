package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncDirection
import com.rahuldharmkar.offlinesynckit.core.SyncRunResult

internal class BidirectionalSyncEngine(
    private val push: suspend () -> SyncRunResult,
    private val pull: suspend () -> SyncRunResult,
    private val config: SyncConfig,
    private val log: (String) -> Unit
) {

    suspend fun sync(): SyncRunResult {
        return when (config.syncDirection) {
            SyncDirection.PUSH -> {
                log("BidirectionalSyncEngine running PUSH sync")
                push()
            }

            SyncDirection.PULL -> {
                log("BidirectionalSyncEngine running PULL sync")
                pull()
            }

            SyncDirection.BOTH -> {
                log("BidirectionalSyncEngine running BOTH sync: PUSH then PULL")

                val pushResult = push()
                val pullResult = pull()

                combine(
                    first = pushResult,
                    second = pullResult
                )
            }
        }
    }

    private fun combine(
        first: SyncRunResult,
        second: SyncRunResult
    ): SyncRunResult {
        return SyncRunResult(
            totalProcessed = first.totalProcessed + second.totalProcessed,
            successCount = first.successCount + second.successCount,
            failedCount = first.failedCount + second.failedCount,
            conflictCount = first.conflictCount + second.conflictCount,
            giveUpCount = first.giveUpCount + second.giveUpCount
        )
    }
}