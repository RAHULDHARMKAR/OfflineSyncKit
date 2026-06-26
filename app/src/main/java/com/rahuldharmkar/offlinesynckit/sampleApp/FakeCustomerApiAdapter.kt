package com.rahuldharmkar.offlinesynckit.sampleApp

import android.util.Log
import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncApiResult
import com.rahuldharmkar.offlinesynckit.core.SyncRequest
import kotlinx.coroutines.delay

class FakeCustomerApiAdapter : SyncApiAdapter {

    override suspend fun sync(
        request: SyncRequest
    ): SyncApiResult {
        delay(800)

        Log.d(
            "FakeCustomerApi",
            "Synced entityName=${request.entityName} entityId=${request.entityId} operation=${request.operation} payload=${request.payload} token=${request.authToken} headers=${request.headers}"
        )

        return SyncApiResult(success = true)
    }
}