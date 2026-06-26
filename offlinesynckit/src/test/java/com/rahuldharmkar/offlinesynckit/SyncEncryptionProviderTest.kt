package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.core.NoOpSyncEncryptionProvider
import com.rahuldharmkar.offlinesynckit.core.SyncEncryptionProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncEncryptionProviderTest {

    @Test
    fun noOpEncryptionProviderShouldReturnOriginalPayload() {
        val payload = """{"name":"Rahul"}"""

        val encrypted = NoOpSyncEncryptionProvider.encrypt(payload)
        val decrypted = NoOpSyncEncryptionProvider.decrypt(encrypted)

        assertEquals(payload, encrypted)
        assertEquals(payload, decrypted)
    }

    @Test
    fun customEncryptionProviderShouldEncryptAndDecryptPayload() {
        val provider = object : SyncEncryptionProvider {
            override fun encrypt(plainText: String): String {
                return plainText.reversed()
            }

            override fun decrypt(cipherText: String): String {
                return cipherText.reversed()
            }
        }

        val payload = """{"name":"Rahul"}"""

        val encrypted = provider.encrypt(payload)
        val decrypted = provider.decrypt(encrypted)

        assertEquals("}\"luhaR\":\"eman\"{", encrypted)
        assertEquals(payload, decrypted)
    }
}