package com.rahuldharmkar.offlinesynckit.core

/**
 * Snapshot of the current synchronization queue.
 *
 * Intended for debugging, diagnostics and developer tools.
 */
data class QueueInspectionReport(

    val pendingItems: List<SyncQueueItem>,

    val syncingItems: List<SyncQueueItem>,

    val failedItems: List<SyncQueueItem>,

    val conflictItems: List<SyncQueueItem>,

    val giveUpItems: List<SyncQueueItem>,

    val syncedItems: List<SyncQueueItem>

)