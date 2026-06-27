package com.rahuldharmkar.offlinesynckit.core

/**
 * Provides tenant or organization information for multi-tenant sync.
 */
fun interface SyncTenantProvider {

    /**
     * Returns the active tenant id.
     */
    fun getTenantId(): String?
}