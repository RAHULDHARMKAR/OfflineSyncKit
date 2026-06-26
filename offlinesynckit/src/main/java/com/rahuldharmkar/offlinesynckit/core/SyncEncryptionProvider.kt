package com.rahuldharmkar.offlinesynckit.core

/**
 * Provides payload encryption and decryption for OfflineSyncKit.
 *
 * Implement this interface when queued payloads should be stored securely
 * instead of plain text.
 */
interface SyncEncryptionProvider {

    /**
     * Encrypts plain text before it is stored in the local queue.
     */
    fun encrypt(plainText: String): String

    /**
     * Decrypts encrypted text before it is sent to the remote server.
     */
    fun decrypt(cipherText: String): String
}