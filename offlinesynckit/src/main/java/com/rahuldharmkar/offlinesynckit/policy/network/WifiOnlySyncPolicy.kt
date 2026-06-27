package com.rahuldharmkar.offlinesynckit.policy.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.rahuldharmkar.offlinesynckit.policy.SyncPolicy

/**
 * A [SyncPolicy] that allows synchronization only when the device
 * is connected to a Wi-Fi network.
 *
 * Mobile data, Ethernet, VPN-only connections, or disconnected states
 * will prevent synchronization.
 */
class WifiOnlySyncPolicy : SyncPolicy {

    override fun canSync(context: Context): Boolean {

        val connectivityManager =
            context.getSystemService(ConnectivityManager::class.java)
                ?: return false

        val network =
            connectivityManager.activeNetwork
                ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(network)
                ?: return false

        return capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI
        )
    }
}