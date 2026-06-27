package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.security.DefaultSyncKeyProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultSyncKeyProviderTest {

    @Test
    fun shouldReturnProvidedSecretKey() {
        val key = "12345678901234567890123456789012"
            .toByteArray(Charsets.UTF_8)

        val provider = DefaultSyncKeyProvider(key)

        val secretKey = provider.getSecretKey()

        assertEquals("AES", secretKey.algorithm)
        assertArrayEquals(key, secretKey.encoded)
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldRejectInvalidKeyLength() {
        DefaultSyncKeyProvider(
            "short".toByteArray(Charsets.UTF_8)
        )
    }
}