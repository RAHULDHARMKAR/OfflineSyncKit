package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.core.SyncEncryptionProvider
import com.rahuldharmkar.offlinesynckit.internal.engine.EncryptionEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class EncryptionEngineTest {

    @Test
    fun encryptionEngineShouldEncryptAndDecryptPayload() {
        val provider = object : SyncEncryptionProvider {
            override fun encrypt(plainText: String): String {
                return "encrypted:$plainText"
            }

            override fun decrypt(cipherText: String): String {
                return cipherText.removePrefix("encrypted:")
            }
        }

        val engine = EncryptionEngine(provider)

        val payload = """{"id":"1"}"""

        val encrypted = engine.encryptPayload(payload)
        val decrypted = engine.decryptPayload(encrypted)

        assertEquals("""encrypted:{"id":"1"}""", encrypted)
        assertEquals(payload, decrypted)

    }

}