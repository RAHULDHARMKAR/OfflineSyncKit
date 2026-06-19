package com.rahuldharmkar.offlinesynckit

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncRetryPolicyTest {

    @Test
    fun `default max retry count should be 3`() {
        val policy = com.rahuldharmkar.offlinesynckit.core.SyncRetryPolicy()

        assertEquals(3, policy.maxRetryCount)
    }

    @Test
    fun `custom max retry count should be accepted`() {
        val policy = com.rahuldharmkar.offlinesynckit.core.SyncRetryPolicy(maxRetryCount = 5)

        assertEquals(5, policy.maxRetryCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative max retry count should throw exception`() {
        com.rahuldharmkar.offlinesynckit.core.SyncRetryPolicy(maxRetryCount = -1)
    }
}