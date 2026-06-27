package com.rahuldharmkar.offlinesynckit.security

import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Default [SyncKeyProvider] backed by raw AES key bytes.
 *
 * Useful for testing, sample apps, and development.
 *
 * For production apps, prefer [AndroidKeystoreKeyProvider].
 */
class DefaultSyncKeyProvider(
    private val secretKey: ByteArray
) : SyncKeyProvider {

    init {
        require(
            secretKey.size == 16 ||
                    secretKey.size == 24 ||
                    secretKey.size == 32
        ) {
            "AES secretKey must be 16, 24, or 32 bytes"
        }
    }

    override fun getSecretKey(): SecretKey {
        return SecretKeySpec(secretKey, AES)
    }

    private companion object {
        private const val AES = "AES"
    }
}