package com.rahuldharmkar.offlinesynckit.core

/**
 * Provides encryption key material for OfflineSyncKit security providers.
 *
 * Applications can implement this interface to retrieve keys from:
 * - Android Keystore
 * - Remote key services
 * - Enterprise vaults
 * - Local secure storage
 */
fun interface SyncKeyProvider {

    /**
     * Returns raw AES key material.
     *
     * Valid AES key sizes are:
     * - 16 bytes for AES-128
     * - 24 bytes for AES-192
     * - 32 bytes for AES-256
     */
    fun getSecretKey(): ByteArray
}