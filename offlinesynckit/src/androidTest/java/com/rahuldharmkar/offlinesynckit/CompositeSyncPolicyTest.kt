package com.rahuldharmkar.offlinesynckit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rahuldharmkar.offlinesynckit.policy.SyncPolicy
import com.rahuldharmkar.offlinesynckit.policy.composite.CompositeSyncPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositeSyncPolicyTest {

    private val context: Context =
        ApplicationProvider.getApplicationContext()

    @Test
    fun allOfShouldReturnTrueWhenAllPoliciesAllowSync() {
        val policy = CompositeSyncPolicy.allOf(
            TestPolicy(true),
            TestPolicy(true)
        )

        assertTrue(policy.canSync(context))
    }

    @Test
    fun allOfShouldReturnFalseWhenAnyPolicyBlocksSync() {
        val policy = CompositeSyncPolicy.allOf(
            TestPolicy(true),
            TestPolicy(false)
        )

        assertFalse(policy.canSync(context))
    }

    @Test
    fun anyOfShouldReturnTrueWhenAnyPolicyAllowsSync() {
        val policy = CompositeSyncPolicy.anyOf(
            TestPolicy(false),
            TestPolicy(true)
        )

        assertTrue(policy.canSync(context))
    }

    @Test
    fun anyOfShouldReturnFalseWhenAllPoliciesBlockSync() {
        val policy = CompositeSyncPolicy.anyOf(
            TestPolicy(false),
            TestPolicy(false)
        )

        assertFalse(policy.canSync(context))
    }

    @Test(expected = IllegalArgumentException::class)
    fun allOfShouldRejectEmptyPolicies() {
        CompositeSyncPolicy.allOf()
    }

    @Test(expected = IllegalArgumentException::class)
    fun anyOfShouldRejectEmptyPolicies() {
        CompositeSyncPolicy.anyOf()
    }

    private class TestPolicy(
        private val result: Boolean
    ) : SyncPolicy {

        override fun canSync(context: Context): Boolean {
            return result
        }
    }
}