package com.rahuldharmkar.offlinesynckit.security

/**
 * Default signature provider that does not sign payloads.
 */
object NoOpSyncSignatureProvider : SyncSignatureProvider {

    override fun sign(payload: String): String {
        return ""
    }
}