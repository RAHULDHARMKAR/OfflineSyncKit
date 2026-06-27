package com.rahuldharmkar.offlinesynckit.security

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * HMAC-SHA256 based signature provider.
 */
class HmacSyncSignatureProvider(
    private val secret: ByteArray
) : SyncSignatureProvider {

    override fun sign(payload: String): String {
        val mac = Mac.getInstance(HMAC_SHA_256)

        mac.init(
            SecretKeySpec(
                secret,
                HMAC_SHA_256
            )
        )

        val signatureBytes = mac.doFinal(
            payload.toByteArray(Charsets.UTF_8)
        )

        return Base64.encodeToString(
            signatureBytes,
            Base64.NO_WRAP
        )
    }

    private companion object {
        private const val HMAC_SHA_256 = "HmacSHA256"
    }
}