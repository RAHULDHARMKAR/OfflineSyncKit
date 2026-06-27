package com.rahuldharmkar.offlinesynckit.security

import javax.crypto.SecretKey

/**
 * Provides secret key material for OfflineSyncKit encryption providers.
 *
 * Implementations may return keys from:
 * - Raw byte arrays
 * - Android Keystore
 * - Enterprise key vaults
 * - Remote key management systems
 */
fun interface SyncKeyProvider {

    /**
     * Returns a [SecretKey] used for encryption and decryption.
     */
    fun getSecretKey(): SecretKey
}