package com.rahuldharmkar.offlinesynckit.policy.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.rahuldharmkar.offlinesynckit.policy.SyncPolicy

/**
 * Allows synchronization only while the device is charging.
 *
 * Supported charging sources:
 * - USB
 * - AC Charger
 * - Wireless Charger
 * - Dock Charger
 */
class ChargingOnlySyncPolicy : SyncPolicy {

    override fun canSync(
        context: Context
    ): Boolean {

        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return false

        val status = batteryIntent.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN
        )

        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }
}