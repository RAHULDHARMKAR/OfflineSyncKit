package com.rahuldharmkar.offlinesynckit.core

/**
 * Default implementation of [SyncKeyProvider].
 *
 * This provider simply returns the supplied AES key.
 *
 * Suitable for:
 * - Sample applications
 * - Development
 * - Testing
 *
 * For production applications consider implementing
 * [SyncKeyProvider] using Android Keystore or another
 * secure key management solution.
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

    override fun getSecretKey(): ByteArray = secretKey
}