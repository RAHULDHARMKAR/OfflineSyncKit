package com.rahuldharmkar.offlinesynckit

import android.content.Context
import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncPullAdapter
import com.rahuldharmkar.offlinesynckit.core.SyncConfig

/**
 * Entry point for creating an [OfflineSyncKit] instance using a builder-style API.
 *
 * `SyncClient.Builder` is the recommended way to configure OfflineSyncKit because it keeps
 * initialization readable and scalable as more configuration options are added.
 *
 * Example:
 *
 * ```kotlin
 * val syncKit = SyncClient.Builder(applicationContext)
 *     .apiAdapter(MySyncApiAdapter())
 *     .config(
 *         SyncConfig(
 *             autoSyncWhenOnline = true
 *         )
 *     )
 *     .build()
 * ```
 */
class SyncClient private constructor() {

    /**
     * Builder used to create an [OfflineSyncKit] instance.
     *
     * The builder requires a [SyncApiAdapter]. Optional configuration can be supplied
     * using [config] and optional pull synchronization can be configured using [pullAdapter].
     *
     * @property context Android application or activity context.
     */
    class Builder(
        private val context: Context
    ) {
        private var apiAdapter: SyncApiAdapter? = null
        private var pullAdapter: SyncPullAdapter? = null
        private var config: SyncConfig = SyncConfig()

        /**
         * Sets the adapter responsible for sending queued sync requests to the remote server.
         *
         * This is required before calling [build].
         *
         * @param adapter Implementation of [SyncApiAdapter].
         * @return The current builder instance.
         */
        fun apiAdapter(
            adapter: SyncApiAdapter
        ) = apply {
            this.apiAdapter = adapter
        }

        /**
         * Sets an optional pull adapter for future pull or bidirectional sync use cases.
         *
         * @param adapter Implementation of [SyncPullAdapter].
         * @return The current builder instance.
         */
        fun pullAdapter(
            adapter: SyncPullAdapter
        ) = apply {
            this.pullAdapter = adapter
        }

        /**
         * Sets advanced SDK configuration such as retry policy, serializers,
         * headers, auth token providers, conflict strategy, and auto-sync behavior.
         *
         * @param config OfflineSyncKit configuration.
         * @return The current builder instance.
         */
        fun config(
            config: SyncConfig
        ) = apply {
            this.config = config
        }

        /**
         * Builds and returns an [OfflineSyncKit] instance.
         *
         * @throws IllegalArgumentException if [apiAdapter] was not provided.
         */
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