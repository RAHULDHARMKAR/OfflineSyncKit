package com.rahuldharmkar.offlinesynckit.sampleApp

import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rahuldharmkar.offlinesynckit.OfflineSyncKit
import com.rahuldharmkar.offlinesynckit.core.QueueInspectionReport
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncQueueFilter
import com.rahuldharmkar.offlinesynckit.core.SyncQueueItem
import com.rahuldharmkar.offlinesynckit.core.SyncStats
import com.rahuldharmkar.offlinesynckit.core.SyncStatus
import kotlinx.coroutines.launch
import com.rahuldharmkar.offlinesynckit.core.SyncHealthReport

@Composable
fun CustomerSyncSampleScreen(
    syncKit: OfflineSyncKit
) {
    val scope = rememberCoroutineScope()

    var queryResults by remember {
        mutableStateOf(emptyList<SyncQueueItem>())
    }

    var healthReport by remember {
        mutableStateOf<SyncHealthReport?>(null)
    }

    var queueInspectionReport by remember {
        mutableStateOf<QueueInspectionReport?>(null)
    }

    val queueItems by syncKit.observeQueue()
        .collectAsState(initial = emptyList())

    val stats by syncKit.observeStats()
        .collectAsState(
            initial = SyncStats(
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

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    queueInspectionReport = syncKit.inspectQueue()
                }
            }
        ) {
            Text("Inspect Queue")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    val currentTime = System.currentTimeMillis()

                    val customers = listOf(
                        Customer(
                            id = "batch_${currentTime}_1",
                            name = "Batch Customer 1",
                            phone = "8888888881"
                        ),
                        Customer(
                            id = "batch_${currentTime}_2",
                            name = "Batch Customer 2",
                            phone = "8888888882"
                        ),
                        Customer(
                            id = "batch_${currentTime}_3",
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
                scope.launch {
                    queryResults = syncKit.queryQueue(
                        SyncQueueFilter(
                            status = SyncStatus.PENDING,
                            entityName = "customer"
                        )
                    )
                }
            }
        ) {
            Text("Query Pending Customers")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    healthReport = syncKit.getHealthReport()
                }
            }
        ) {
            Text("Show Health Report")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    queryResults = syncKit.queryQueue(
                        SyncQueueFilter(
                            status = SyncStatus.FAILED
                        )
                    )
                }
            }
        ) {
            Text("Query Failed Items")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    queryResults = syncKit.queryQueue(
                        SyncQueueFilter(
                            status = SyncStatus.CONFLICT
                        )
                    )
                }
            }
        ) {
            Text("Query Conflicts")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {

                    val report =
                        syncKit.inspectQueue()

                    Log.d(
                        "QueueInspector",
                        report.toString()
                    )
                }
            }
        ) {
            Text("Inspect Queue")
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

        healthReport?.let { report ->
            HealthReportCard(report)
        }

        queueInspectionReport?.let { report ->
            QueueInspectionCard(report)
        }

        StatsCard(stats)

        Text(
            text = "Query Results",
            style = MaterialTheme.typography.titleMedium
        )

        queryResults.forEach { item ->
            QueueItemCard(item)
        }

        Text(
            text = "Queue Items",
            style = MaterialTheme.typography.titleMedium
        )

        queueItems.forEach { item ->
            QueueItemCard(item)
        }
    }
}

@Composable
private fun QueueItemCard(
    item: SyncQueueItem
) {
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

@Composable
private fun QueueInspectionCard(
    report: QueueInspectionReport
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Queue Inspection",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Pending: ${report.pendingItems.size}")
            Text("Syncing: ${report.syncingItems.size}")
            Text("Synced: ${report.syncedItems.size}")
            Text("Failed: ${report.failedItems.size}")
            Text("Conflict: ${report.conflictItems.size}")
            Text("Give Up: ${report.giveUpItems.size}")
        }
    }
}

@Composable
private fun HealthReportCard(
    report: SyncHealthReport
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Sync Health Report",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Total Queue Items: ${report.totalQueueItems}")
            Text("Pending: ${report.pendingCount}")
            Text("Syncing: ${report.syncingCount}")
            Text("Synced: ${report.syncedCount}")
            Text("Failed: ${report.failedCount}")
            Text("Conflict: ${report.conflictCount}")
            Text("Give Up: ${report.giveUpCount}")
            Text("Sync Paused: ${report.isSyncPaused}")
            Text("Sync Direction: ${report.syncDirection}")
            Text("Policy Allowed: ${report.isPolicyAllowed}")
        }
    }
}

@Composable
private fun StatsCard(
    stats: SyncStats
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