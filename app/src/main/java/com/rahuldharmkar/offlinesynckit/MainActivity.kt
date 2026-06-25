package com.rahuldharmkar.offlinesynckit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent



import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncConflictResolution
import com.rahuldharmkar.offlinesynckit.core.SyncConflictResolver
import com.rahuldharmkar.offlinesynckit.core.SyncConflictStrategy
import com.rahuldharmkar.offlinesynckit.core.SyncEventListener
import com.rahuldharmkar.offlinesynckit.core.SyncLogger
import com.rahuldharmkar.offlinesynckit.core.SyncMergePolicy
import com.rahuldharmkar.offlinesynckit.core.SyncRetryPolicy
import com.rahuldharmkar.offlinesynckit.sampleApp.CustomerSyncSampleScreen
import com.rahuldharmkar.offlinesynckit.sampleApp.FakeCustomerApiAdapter
import com.rahuldharmkar.offlinesynckit.core.SyncSerializer
import com.rahuldharmkar.offlinesynckit.core.SyncSerializerRegistry
import com.rahuldharmkar.offlinesynckit.sampleApp.Customer
import com.rahuldharmkar.offlinesynckit.sampleApp.CustomerJsonSerializer

class MainActivity : ComponentActivity() {

    private lateinit var syncKit: OfflineSyncKit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serializerRegistry = SyncSerializerRegistry().apply {
            register(
                Customer::class,
                SyncSerializer<Customer> { customer ->
                    CustomerJsonSerializer.toJson(customer)
                }
            )
        }

        syncKit = OfflineSyncKit.create(
            context = applicationContext,
            apiAdapter = FakeCustomerApiAdapter(),
            config = SyncConfig(
                retryPolicy = SyncRetryPolicy(maxRetryCount = 3),
                syncBatchSize = 20,
                enablePeriodicSync = true,
                periodicSyncIntervalMinutes = 15,
                autoSyncWhenOnline = true,
                mergePolicy = SyncMergePolicy.APPEND_ONLY,
                conflictStrategy = SyncConflictStrategy.MANUAL,
                conflictResolver = SyncConflictResolver {
                    SyncConflictResolution.MarkManual
                },
                logger = SyncLogger { message ->
                    Log.d("OfflineSyncKit", message)
                },
                eventListener = SyncEventListener { event ->
                    Log.d("OfflineSyncEvent", event.toString())
                },
                serializerRegistry = serializerRegistry,
            )
        )

        setContent {
            CustomerSyncSampleScreen(syncKit = syncKit)
        }
    }
}