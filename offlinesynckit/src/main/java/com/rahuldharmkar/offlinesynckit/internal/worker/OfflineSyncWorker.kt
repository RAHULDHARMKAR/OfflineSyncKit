package com.rahuldharmkar.offlinesynckit.internal.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rahuldharmkar.offlinesynckit.OfflineSyncConfig
import com.rahuldharmkar.offlinesynckit.OfflineSyncKit

internal class OfflineSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {

            val syncKit = OfflineSyncKit.create(
                context = applicationContext,
                apiAdapter = OfflineSyncConfig.getApiAdapter(),
                pullAdapter = OfflineSyncConfig.getPullAdapter(),
                config = OfflineSyncConfig.getSyncConfig()
            )

            syncKit.syncNow()

            Result.success()

        } catch (e: Exception) {
            Log.e("OfflineSyncWorker", "Auto sync failed", e)
            Result.retry()
        }
    }
}