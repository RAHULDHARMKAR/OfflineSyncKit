package com.rahuldharmkar.offlinesynckit.policy

import android.content.Context

/**
 * Defines whether synchronization is allowed to run.
 */
fun interface SyncPolicy {

    /**
     * Returns true when sync is allowed.
     */
    fun canSync(context: Context): Boolean
}