package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.security.HmacSyncSignatureProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HmacSyncSignatureProviderTest {

    @Test
    fun signShouldReturnNonEmptySignature() {
        val provider = HmacSyncSignatureProvider(
            secret = "my-secret-key".toByteArray(Charsets.UTF_8)
        )

        val signature = provider.sign("""{"id":"1"}""")

        assertTrue(signature.isNotBlank())
    }

    @Test
    fun samePayloadAndSameSecretShouldReturnSameSignature() {
        val provider = HmacSyncSignatureProvider(
            secret = "my-secret-key".toByteArray(Charsets.UTF_8)
        )

        val payload = """{"id":"1"}"""

        val signatureOne = provider.sign(payload)
        val signatureTwo = provider.sign(payload)

        assertEquals(signatureOne, signatureTwo)
    }

    @Test
    fun differentPayloadShouldReturnDifferentSignature() {
        val provider = HmacSyncSignatureProvider(
            secret = "my-secret-key".toByteArray(Charsets.UTF_8)
        )

        val signatureOne = provider.sign("""{"id":"1"}""")
        val signatureTwo = provider.sign("""{"id":"2"}""")

        assertNotEquals(signatureOne, signatureTwo)
    }

    @Test
    fun differentSecretShouldReturnDifferentSignature() {
        val payload = """{"id":"1"}"""

        val providerOne = HmacSyncSignatureProvider(
            secret = "secret-one".toByteArray(Charsets.UTF_8)
        )

        val providerTwo = HmacSyncSignatureProvider(
            secret = "secret-two".toByteArray(Charsets.UTF_8)
        )

        assertNotEquals(
            providerOne.sign(payload),
            providerTwo.sign(payload)
        )
    }
}