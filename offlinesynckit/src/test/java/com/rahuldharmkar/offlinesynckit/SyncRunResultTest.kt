package com.rahuldharmkar.offlinesynckit

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncRunResultTest {

    @Test
    fun `empty result should contain zero counts`() {
        val result = com.rahuldharmkar.offlinesynckit.core.SyncRunResult.empty()

        assertEquals(0, result.totalProcessed)
        assertEquals(0, result.successCount)
        assertEquals(0, result.failedCount)
        assertEquals(0, result.conflictCount)
        assertEquals(0, result.giveUpCount)
    }
}