package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.core.DefaultSyncKeyProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class DefaultSyncKeyProviderTest {

    @Test
    fun shouldReturnProvidedSecretKey() {

        val key =
            "12345678901234567890123456789012"
                .toByteArray()

        val provider = DefaultSyncKeyProvider(key)

        assertArrayEquals(
            key,
            provider.getSecretKey()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldRejectInvalidKeyLength() {

        DefaultSyncKeyProvider(
            "short".toByteArray()
        )
    }
}