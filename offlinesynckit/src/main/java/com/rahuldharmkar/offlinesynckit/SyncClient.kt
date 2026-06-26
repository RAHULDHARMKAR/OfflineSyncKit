package com.rahuldharmkar.offlinesynckit

import android.content.Context
import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter
import com.rahuldharmkar.offlinesynckit.core.SyncConfig

class SyncClient private constructor() {

    class Builder(
        private val context: Context
    ) {
        private var apiAdapter: SyncApiAdapter? = null
        private var pullAdapter: SyncPullAdapter? = null
        private var config: SyncConfig = SyncConfig()

        fun apiAdapter(
            adapter: SyncApiAdapter
        ) = apply {
            this.apiAdapter = adapter
        }

        fun pullAdapter(
            adapter: SyncPullAdapter
        ) = apply {
            this.pullAdapter = adapter
        }

        fun config(
            config: SyncConfig
        ) = apply {
            this.config = config
        }

        fun build(): OfflineSyncKit {
            val adapter = requireNotNull(apiAdapter) {
                """
    SyncApiAdapter is required.

    Example:

    SyncClient.Builder(context)
        .apiAdapter(MyApiAdapter())
        .build()
    """.trimIndent()
            }

            return OfflineSyncKit.create(
                context = context.applicationContext,
                apiAdapter = adapter,
                pullAdapter = pullAdapter,
                config = config
            )
        }
    }
}