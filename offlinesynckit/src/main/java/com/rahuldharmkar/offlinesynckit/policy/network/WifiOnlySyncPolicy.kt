package com.rahuldharmkar.offlinesynckit.policy.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.rahuldharmkar.offlinesynckit.policy.SyncPolicy

/**
 * Allows synchronization only when the active network
 * is connected through Wi-Fi.
 *
 * Returns false when:
 * - Device is offline
 * - Mobile data is active
 * - Ethernet is active
 * - VPN without Wi-Fi
 */
class WifiOnlySyncPolicy : SyncPolicy {

    override fun canSync(
        context: Context
    ): Boolean {

        val connectivityManager =
            context.getSystemService(
                ConnectivityManager::class.java
            ) ?: return false

        val activeNetwork =
            connectivityManager.activeNetwork
                ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(
                activeNetwork
            ) ?: return false

        return capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI
        )
    }
}