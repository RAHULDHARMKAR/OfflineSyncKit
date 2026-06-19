package com.rahuldharmkar.offlinesynckit.sampleApp

import android.util.Log
import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncApiResult
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import kotlinx.coroutines.delay

class FakeCustomerApiAdapter : com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter {

    override suspend fun sync(
        entityName: String,
        entityId: String,
        operation: com.rahuldharmkar.offlinesynckit.core.SyncOperation,
        payload: String
    ): com.rahuldharmkar.offlinesynckit.api.SyncApiResult {
        delay(800)

        Log.d(
            "FakeCustomerApi",
            "Synced entityName=$entityName entityId=$entityId operation=$operation payload=$payload"
        )

        return com.rahuldharmkar.offlinesynckit.api.SyncApiResult(success = true)
    }
}