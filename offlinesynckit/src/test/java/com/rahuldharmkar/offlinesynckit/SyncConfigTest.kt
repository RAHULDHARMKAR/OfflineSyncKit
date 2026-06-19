package com.rahuldharmkar.offlinesynckit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SyncConfigTest {

    @Test
    fun `default config should have expected values`() {
        val config = com.rahuldharmkar.offlinesynckit.core.SyncConfig()

        assertEquals(20, config.syncBatchSize)
        assertFalse(config.enablePeriodicSync)
        assertEquals(15L, config.periodicSyncIntervalMinutes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `sync batch size less than 1 should throw exception`() {
        com.rahuldharmkar.offlinesynckit.core.SyncConfig(syncBatchSize = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `periodic sync interval less than 15 should throw exception`() {
        com.rahuldharmkar.offlinesynckit.core.SyncConfig(periodicSyncIntervalMinutes = 5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `stale syncing timeout less than 1 should throw exception`() {
        com.rahuldharmkar.offlinesynckit.core.SyncConfig(staleSyncingTimeoutMinutes = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `synced retention less than 1 should throw exception`() {
        com.rahuldharmkar.offlinesynckit.core.SyncConfig(syncedItemRetentionMinutes = 0)
    }
}