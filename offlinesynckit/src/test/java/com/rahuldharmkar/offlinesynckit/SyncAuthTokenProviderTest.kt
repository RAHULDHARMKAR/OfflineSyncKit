package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.core.SyncAuthTokenProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncAuthTokenProviderTest {

    @Test
    fun `should return auth token`() = runBlocking {

        val provider = SyncAuthTokenProvider {
            "Bearer my-secret-token"
        }

        val token = provider.getToken()

        assertEquals(
            "Bearer my-secret-token",
            token
        )
    }

    @Test
    fun `should support null token`() = runBlocking {

        val provider = SyncAuthTokenProvider {
            null
        }

        val token = provider.getToken()

        assertEquals(null, token)
    }
}