package com.rahuldharmkar.offlinesynckit.api

import com.rahuldharmkar.offlinesynckit.core.SyncRequest

/**
 * Adapter responsible for sending synchronization requests to the application's backend.
 *
 * Every application integrating OfflineSyncKit must provide an implementation
 * of this interface.
 *
 * The SDK invokes [sync] whenever queued items need to be synchronized.
 *
 * Example:
 *
 * ```kotlin
 * val adapter = SyncApiAdapter { request ->
 *
 *     val response = api.createCustomer(
 *         request.payload
 *     )
 *
 *     SyncApiResult(
 *         success = response.isSuccessful
 *     )
 * }
 * ```
 *
 * This adapter is transport-agnostic and can be used with:
 *
 * - Retrofit
 * - Ktor
 * - Volley
 * - OkHttp
 * - GraphQL
 * - gRPC
 * - Custom networking stacks
 */
fun interface SyncApiAdapter {

    /**
     * Synchronizes a single queued item with the remote server.
     *
     * This method is called by OfflineSyncKit during synchronization.
     *
     * Returning a successful [SyncApiResult] removes the item from the pending
     * queue (or marks it as synced depending on configuration).
     *
     * Returning a failed result causes the retry policy to be applied.
     *
     * Returning a conflict result delegates handling to the configured
     * conflict resolution strategy.
     *
     * @param request Complete synchronization request including payload,
     * authentication token, custom headers and metadata.
     *
     * @return Result of the synchronization operation.
     */
    suspend fun sync(
        request: SyncRequest
    ): SyncApiResult
}

/**
 * Result returned by [SyncApiAdapter.sync].
 *
 * This tells OfflineSyncKit whether the synchronization completed successfully,
 * failed, or encountered a conflict.
 */
data class SyncApiResult(

    /**
     * Indicates whether synchronization completed successfully.
     */
    val success: Boolean,

    /**
     * Optional error message describing the reason for failure.
     *
     * Used for retry logging and debugging.
     */
    val errorMessage: String? = null,

    /**
     * Indicates that the server detected a synchronization conflict.
     *
     * When true, OfflineSyncKit invokes the configured conflict strategy or
     * custom conflict resolver.
     */
    val isConflict: Boolean = false,

    /**
     * Optional payload returned by the server during conflict resolution.
     *
     * This may contain the latest server representation of the entity,
     * allowing custom merge strategies to resolve differences.
     */
    val serverPayload: String? = null
)