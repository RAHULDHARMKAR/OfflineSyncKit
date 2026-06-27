package com.rahuldharmkar.offlinesynckit.policy

import android.content.Context

/**
 * Default sync policy that always allows sync.
 */
object AlwaysSyncPolicy : SyncPolicy {

    override fun canSync(context: Context): Boolean {
        return true
    }
}