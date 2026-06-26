package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.core.SyncHeaderProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncHeaderProviderTest {

    @Test
    fun `should return custom headers`() = runBlocking {

        val provider = SyncHeaderProvider {

            mapOf(
                "Authorization" to "Bearer token",
                "X-App-Version" to "1.2.0",
                "X-Device" to "Android"
            )
        }

        val headers = provider.getHeaders()

        assertEquals(
            "Bearer token",
            headers["Authorization"]
        )

        assertEquals(
            "1.2.0",
            headers["X-App-Version"]
        )

        assertEquals(
            "Android",
            headers["X-Device"]
        )
    }

    @Test
    fun `should return empty headers`() = runBlocking {

        val provider = SyncHeaderProvider {
            emptyMap()
        }

        val headers = provider.getHeaders()

        assertEquals(
            0,
            headers.size
        )
    }
}