package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter
import com.rahuldharmkar.offlinesynckit.core.SyncConfig

internal object OfflineSyncConfig {

    private var apiAdapter: SyncApiAdapter? = null
    private var pullAdapter: SyncPullAdapter? = null
    private var syncConfig: SyncConfig = SyncConfig()

    fun initialize(
        adapter: SyncApiAdapter,
        pullAdapter: SyncPullAdapter?,
        config: SyncConfig
    ) {
        apiAdapter = adapter
        this.pullAdapter = pullAdapter
        syncConfig = config
    }

    fun getApiAdapter(): SyncApiAdapter {
        return apiAdapter ?: error(
            "OfflineSyncKit is not initialized. Call OfflineSyncKit.create() first."
        )
    }

    fun getPullAdapter(): SyncPullAdapter? {
        return pullAdapter
    }

    fun getSyncConfig(): SyncConfig {
        return syncConfig
    }
}