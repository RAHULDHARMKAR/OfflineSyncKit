package com.rahuldharmkar.offlinesynckit.sampleApp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rahuldharmkar.offlinesynckit.OfflineSyncKit
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncStats
import kotlinx.coroutines.launch

@Composable
fun CustomerSyncSampleScreen(
    syncKit: com.rahuldharmkar.offlinesynckit.OfflineSyncKit
) {
    val scope = rememberCoroutineScope()

    val queueItems by syncKit.observeQueue()
        .collectAsState(initial = emptyList())

    val stats by syncKit.observeStats()
        .collectAsState(
            initial = com.rahuldharmkar.offlinesynckit.core.SyncStats(
                pendingCount = 0,
                syncingCount = 0,
                syncedCount = 0,
                failedCount = 0,
                conflictCount = 0,
                giveUpCount = 0,
                totalCount = 0
            )
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "OfflineSyncKit Sample",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Customer Offline Sync Demo",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    val customer = Customer(
                        id = System.currentTimeMillis().toString(),
                        name = "Rahul Customer",
                        phone = "9999999999"
                    )

                    syncKit.enqueueObjectAndSyncIfOnline(
                        entityName = "customer",
                        entityId = customer.id,
                        operation = SyncOperation.CREATE,
                        entity = customer,
                        type = Customer::class
                    )
                }
            }
        ) {
            Text("Add Customer Offline")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    val customers = listOf(
                        Customer(
                            id = "batch_${System.currentTimeMillis()}_1",
                            name = "Batch Customer 1",
                            phone = "8888888881"
                        ),
                        Customer(
                            id = "batch_${System.currentTimeMillis()}_2",
                            name = "Batch Customer 2",
                            phone = "8888888882"
                        ),
                        Customer(
                            id = "batch_${System.currentTimeMillis()}_3",
                            name = "Batch Customer 3",
                            phone = "8888888883"
                        )
                    )

                    syncKit.enqueueObjectsAndSyncIfOnline(
                        entityName = "customer",
                        items = customers,
                        operation = SyncOperation.CREATE,
                        type = Customer::class,
                        entityIdProvider = { it.id }
                    )
                }
            }
        ) {
            Text("Add Batch Customers")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    syncKit.syncNow()
                }
            }
        ) {
            Text("Manual Sync Now")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                syncKit.pauseSync()
            }
        ) {
            Text("Pause Sync")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                syncKit.resumeSync()
            }
        ) {
            Text("Resume Sync")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    syncKit.retryAllGiveUpItems()
                }
            }
        ) {
            Text("Retry GIVE_UP Items")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    syncKit.clearSynced()
                }
            }
        ) {
            Text("Clear Synced Items")
        }

        StatsCard(stats)

        Text(
            text = "Queue Items",
            style = MaterialTheme.typography.titleMedium
        )

        queueItems.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Entity: ${item.entityName}")
                    Text("ID: ${item.entityId}")
                    Text("Operation: ${item.operation}")
                    Text("Status: ${item.status}")
                    Text("Retry Count: ${item.retryCount}")

                    item.lastError?.let {
                        Text("Error: $it")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(
    stats: com.rahuldharmkar.offlinesynckit.core.SyncStats
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Sync Stats",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Pending: ${stats.pendingCount}")
            Text("Syncing: ${stats.syncingCount}")
            Text("Synced: ${stats.syncedCount}")
            Text("Failed: ${stats.failedCount}")
            Text("Conflict: ${stats.conflictCount}")
            Text("Give Up: ${stats.giveUpCount}")
            Text("Total: ${stats.totalCount}")
        }
    }
}