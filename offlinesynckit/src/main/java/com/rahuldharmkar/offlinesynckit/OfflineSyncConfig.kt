package com.rahuldharmkar.offlinesynckit

internal object OfflineSyncConfig {

    private var apiAdapter: com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter? = null
    private var pullAdapter: com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter? = null
    private var syncConfig: com.rahuldharmkar.offlinesynckit.core.SyncConfig =
        com.rahuldharmkar.offlinesynckit.core.SyncConfig()

    fun initialize(
        adapter: com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter,
        pullAdapter: com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter?,
        config: com.rahuldharmkar.offlinesynckit.core.SyncConfig
    ) {
        apiAdapter = adapter
        OfflineSyncConfig.pullAdapter = pullAdapter
        syncConfig = config
    }

    fun getApiAdapter(): com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter {
        return apiAdapter ?: error(
            "OfflineSyncKit is not initialized. Call OfflineSyncKit.create() first."
        )
    }

    fun getPullAdapter(): com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter? {
        return pullAdapter
    }

    fun getSyncConfig(): com.rahuldharmkar.offlinesynckit.core.SyncConfig {
        return syncConfig
    }
}