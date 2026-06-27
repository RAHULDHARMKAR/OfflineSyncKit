package com.rahuldharmkar.offlinesynckit.security

import com.rahuldharmkar.offlinesynckit.core.SyncConfig

/**
 * Coordinates all security operations for OfflineSyncKit.
 *
 * This manager centralizes encryption, decryption and payload signing,
 * keeping SyncEngine focused on synchronization rather than security.
 */
internal class SyncSecurityManager(
    private val config: SyncConfig
) {

    /**
     * Encrypts a payload before storing it locally.
     */
    fun encrypt(payload: String): String {
        return config.encryptionProvider.encrypt(payload)
    }

    /**
     * Decrypts a payload before sending it to the server.
     */
    fun decrypt(payload: String): String {
        return config.encryptionProvider.decrypt(payload)
    }

    /**
     * Generates security headers for a payload.
     */
    fun createHeaders(
        payload: String,
        headers: Map<String, String>
    ): Map<String, String> {

        val signature =
            config.signatureProvider.sign(payload)

        return if (signature.isBlank()) {
            headers
        } else {
            headers + (
                    HEADER_SIGNATURE to signature
                    )
        }
    }

    private companion object {
        private const val HEADER_SIGNATURE =
            "X-Sync-Signature"
    }
}