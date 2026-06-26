package com.rahuldharmkar.offlinesynckit.internal.engine

import com.rahuldharmkar.offlinesynckit.core.SyncEncryptionProvider

internal class EncryptionEngine(
    private val encryptionProvider: SyncEncryptionProvider
) {

    fun encryptPayload(payload: String): String {
        return encryptionProvider.encrypt(payload)
    }

    fun decryptPayload(payload: String): String {
        return encryptionProvider.decrypt(payload)
    }
}