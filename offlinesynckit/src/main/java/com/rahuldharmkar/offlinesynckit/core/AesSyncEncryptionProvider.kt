package com.rahuldharmkar.offlinesynckit.core

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-GCM based encryption provider for OfflineSyncKit payloads.
 *
 * This provider encrypts payloads before they are stored in the local queue
 * and decrypts them before they are sent to the remote server.
 *
 * AES-GCM provides authenticated encryption, meaning it protects both
 * confidentiality and integrity of the encrypted payload.
 *
 * @param keyProvider Provider that supplies AES key material.
 */
class AesSyncEncryptionProvider(
    private val keyProvider: SyncKeyProvider
) : SyncEncryptionProvider {

    override fun encrypt(plainText: String): String {
        val iv = ByteArray(GCM_IV_SIZE_BYTES)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            createKeySpec(),
            GCMParameterSpec(GCM_TAG_SIZE_BITS, iv)
        )

        val encryptedBytes = cipher.doFinal(
            plainText.toByteArray(Charsets.UTF_8)
        )

        val combined = iv + encryptedBytes

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )
    }

    override fun decrypt(cipherText: String): String {
        val combined = Base64.decode(
            cipherText,
            Base64.NO_WRAP
        )

        require(combined.size > GCM_IV_SIZE_BYTES) {
            "Invalid encrypted payload"
        }

        val iv = combined.copyOfRange(
            0,
            GCM_IV_SIZE_BYTES
        )

        val encryptedBytes = combined.copyOfRange(
            GCM_IV_SIZE_BYTES,
            combined.size
        )

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            createKeySpec(),
            GCMParameterSpec(GCM_TAG_SIZE_BITS, iv)
        )

        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return decryptedBytes.toString(Charsets.UTF_8)
    }

    private fun createKeySpec(): SecretKeySpec {
        val secretKey = keyProvider.getSecretKey()

        require(
            secretKey.size == AES_128_KEY_SIZE_BYTES ||
                    secretKey.size == AES_192_KEY_SIZE_BYTES ||
                    secretKey.size == AES_256_KEY_SIZE_BYTES
        ) {
            "AES secretKey must be 16, 24, or 32 bytes"
        }

        return SecretKeySpec(secretKey, AES)
    }

    companion object {
        private const val AES = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private const val AES_128_KEY_SIZE_BYTES = 16
        private const val AES_192_KEY_SIZE_BYTES = 24
        private const val AES_256_KEY_SIZE_BYTES = 32

        private const val GCM_IV_SIZE_BYTES = 12
        private const val GCM_TAG_SIZE_BITS = 128

        private val secureRandom = SecureRandom()
    }
}