package com.rahuldharmkar.offlinesynckit.core

import com.rahuldharmkar.offlinesynckit.security.NoOpSyncSignatureProvider
import com.rahuldharmkar.offlinesynckit.security.SyncSignatureProvider
import com.rahuldharmkar.offlinesynckit.policy.AlwaysSyncPolicy
import com.rahuldharmkar.offlinesynckit.policy.SyncPolicy

/**
 * Configuration object for OfflineSyncKit.
 *
 * [SyncConfig] controls how synchronization behaves, including retry rules,
 * auto-sync behavior, merge policy, conflict handling, serialization,
 * authentication, headers, logging, events, and interceptors.
 *
 * It is usually passed through [com.rahuldharmkar.offlinesynckit.SyncClient.Builder.config].
 */
data class SyncConfig(

    /**
     * Defines how failed sync attempts should be retried.
     */
    val retryPolicy: SyncRetryPolicy = SyncRetryPolicy(),

    /**
     * Maximum number of queue items processed in a single sync run.
     */
    val syncBatchSize: Int = 20,

    /**
     * Enables periodic background sync using WorkManager.
     */
    val enablePeriodicSync: Boolean = false,

    /**
     * Periodic sync interval in minutes.
     *
     * Android WorkManager requires a minimum interval of 15 minutes.
     */
    val periodicSyncIntervalMinutes: Long = 15L,

    /**
     * Automatically schedules sync when an item is enqueued and network is available.
     */
    val autoSyncWhenOnline: Boolean = true,

    /**
     * Defines how duplicate pending operations for the same entity are handled.
     */
    val mergePolicy: SyncMergePolicy = SyncMergePolicy.APPEND_ONLY,

    /**
     * Listener for sync lifecycle events such as enqueued, started, success,
     * failed, conflict, and give up.
     */
    val eventListener: SyncEventListener? = null,

    /**
     * Optional logger used internally by OfflineSyncKit.
     */
    val logger: SyncLogger? = null,

    /**
     * Timeout after which items stuck in SYNCING status are reset.
     */
    val staleSyncingTimeoutMinutes: Long = 10L,

    /**
     * Automatically removes old synced items after a sync run.
     */
    val autoClearSyncedItems: Boolean = false,

    /**
     * Retention period for synced items before they are eligible for cleanup.
     */
    val syncedItemRetentionMinutes: Long = 60L,

    /**
     * Initial paused state of the sync engine.
     */
    val syncPaused: Boolean = false,

    /**
     * Defines whether the SDK performs push sync, pull sync, or both.
     */
    val syncDirection: SyncDirection = SyncDirection.PUSH,

    /**
     * Default strategy used when the server reports a conflict.
     */
    val conflictStrategy: SyncConflictStrategy = SyncConflictStrategy.MANUAL,

    /**
     * Optional custom conflict resolver for application-specific conflict handling.
     */
    val conflictResolver: SyncConflictResolver? = null,

    /**
     * Registry used to serialize domain objects before adding them to the queue.
     */
    val serializerRegistry: SyncSerializerRegistry = SyncSerializerRegistry(),

    /**
     * Encryption provider used to secure payloads before they are stored
     * in the local offline queue.
     *
     * The same provider is also responsible for decrypting payloads before
     * they are sent to the remote server.
     *
     * By default, [NoOpSyncEncryptionProvider] is used, which performs no
     * encryption and preserves the current SDK behavior.
     *
     * Applications requiring encrypted local storage can provide a custom
     * implementation, such as AES, Android Keystore, or any enterprise
     * encryption solution.
     */
    val encryptionProvider: SyncEncryptionProvider = NoOpSyncEncryptionProvider,

    /**
     * Signature provider used to generate a payload signature before sync.
     *
     * The generated signature is added to [SyncRequest.headers] using
     * the `X-Sync-Signature` header.
     *
     * By default, [NoOpSyncSignatureProvider] is used and no signature
     * is generated.
     */
    val signatureProvider: SyncSignatureProvider = NoOpSyncSignatureProvider,

    /**
     * Interceptors that can observe or customize sync lifecycle behavior.
     */
    val interceptors: List<SyncInterceptor> = emptyList(),

    /**
     * Provides an optional authentication token for each sync request.
     */
    val authTokenProvider: SyncAuthTokenProvider? = null,

    /**
     * Provides optional custom headers for each sync request.
     */
    val headerProvider: SyncHeaderProvider? = null,

    /**
     * Policy used to decide whether synchronization is allowed to run.
     *
     * By default, [AlwaysSyncPolicy] allows sync whenever it is triggered.
     */
    val syncPolicy: SyncPolicy = AlwaysSyncPolicy,

    /**
     * Provides tenant information for multi-tenant synchronization.
     */
    val tenantProvider: SyncTenantProvider? = null,

    /**
     * Optional handler invoked when remote items are pulled successfully.
     *
     * Apps can use this callback to apply pulled items to their own
     * local database, cache, or repository layer.
     */
    val pullDataHandler: SyncPullDataHandler? = null,

) {
    init {
        require(syncBatchSize > 0) {
            "syncBatchSize must be greater than 0"
        }

        require(periodicSyncIntervalMinutes >= 15L) {
            "periodicSyncIntervalMinutes must be at least 15 minutes"
        }

        require(staleSyncingTimeoutMinutes > 0) {
            "staleSyncingTimeoutMinutes must be greater than 0"
        }

        require(syncedItemRetentionMinutes > 0) {
            "syncedItemRetentionMinutes must be greater than 0"
        }
    }
}