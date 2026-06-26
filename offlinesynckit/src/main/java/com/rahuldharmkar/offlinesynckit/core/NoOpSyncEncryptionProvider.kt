package com.rahuldharmkar.offlinesynckit.core

/**
 * Default encryption provider that does not modify payloads.
 *
 * This keeps OfflineSyncKit backward compatible when encryption is not enabled.
 */
object NoOpSyncEncryptionProvider : SyncEncryptionProvider {

    override fun encrypt(plainText: String): String {
        return plainText
    }

    override fun decrypt(cipherText: String): String {
        return cipherText
    }
}