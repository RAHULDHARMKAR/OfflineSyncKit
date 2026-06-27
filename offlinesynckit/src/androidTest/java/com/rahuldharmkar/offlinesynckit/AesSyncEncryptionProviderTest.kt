package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.security.AesSyncEncryptionProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AesSyncEncryptionProviderTest {

    @Test
    fun encryptShouldReturnDifferentValueFromPlainText() {
        val provider = AesSyncEncryptionProvider(
            secretKey = "12345678901234567890123456789012"
                .toByteArray(Charsets.UTF_8)
        )

        val plainText = """{"name":"Rahul","phone":"9999999999"}"""

        val encrypted = provider.encrypt(plainText)

        assertNotEquals(plainText, encrypted)
        assertTrue(encrypted.isNotBlank())
    }

    @Test
    fun decryptShouldReturnOriginalPlainText() {
        val provider = AesSyncEncryptionProvider(
            secretKey = "12345678901234567890123456789012"
                .toByteArray(Charsets.UTF_8)
        )

        val plainText = """{"name":"Rahul","phone":"9999999999"}"""

        val encrypted = provider.encrypt(plainText)
        val decrypted = provider.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun samePayloadShouldGenerateDifferentEncryptedValues() {
        val provider = AesSyncEncryptionProvider(
            secretKey = "12345678901234567890123456789012"
                .toByteArray(Charsets.UTF_8)
        )

        val plainText = """{"id":"1"}"""

        val encryptedOne = provider.encrypt(plainText)
        val encryptedTwo = provider.encrypt(plainText)

        assertNotEquals(encryptedOne, encryptedTwo)
    }

    @Test
    fun invalidKeySizeShouldThrowException() {
        val exception = runCatching {
            AesSyncEncryptionProvider(
                secretKey = "short-key".toByteArray(Charsets.UTF_8)
            )
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
        assertEquals(
            "AES secretKey must be 16, 24, or 32 bytes",
            exception?.message
        )
    }
}