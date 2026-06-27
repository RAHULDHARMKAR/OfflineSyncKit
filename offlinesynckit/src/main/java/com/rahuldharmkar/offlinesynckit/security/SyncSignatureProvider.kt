package com.rahuldharmkar.offlinesynckit.security

/**
 * Provides a cryptographic signature for sync payloads.
 *
 * A signature can be used by backend APIs to verify that the payload
 * was not modified or tampered with before reaching the server.
 */
fun interface SyncSignatureProvider {

    /**
     * Creates a signature for the given payload.
     */
    fun sign(payload: String): String
}