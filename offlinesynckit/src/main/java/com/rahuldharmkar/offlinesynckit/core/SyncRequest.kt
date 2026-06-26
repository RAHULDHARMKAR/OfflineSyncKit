package com.rahuldharmkar.offlinesynckit.core

/**
 * Represents a single synchronization request sent from OfflineSyncKit
 * to the application's backend.
 *
 * A [SyncRequest] contains all information required by the
 * [SyncApiAdapter] to perform a synchronization operation.
 *
 * Besides the entity payload, it also carries authentication,
 * custom headers and metadata that can be used by enterprise APIs.
 *
 * Typical flow:
 *
 * Queue Item
 * ↓
 * SyncRequest
 * ↓
 * SyncApiAdapter
 * ↓
 * Your REST API / GraphQL API / Backend
 */
data class SyncRequest(

    /**
     * Name of the entity being synchronized.
     *
     * Examples:
     * - customer
     * - order
     * - invoice
     * - product
     */
    val entityName: String,

    /**
     * Unique identifier of the entity.
     *
     * This value should remain stable throughout the entity lifecycle.
     */
    val entityId: String,

    /**
     * Synchronization operation.
     *
     * Examples:
     * - CREATE
     * - UPDATE
     * - DELETE
     */
    val operation: SyncOperation,

    /**
     * Serialized payload that will be sent to the server.
     *
     * Usually this is a JSON string produced by a serializer.
     */
    val payload: String,

    /**
     * Optional authentication token supplied by
     * [SyncAuthTokenProvider].
     *
     * Example:
     *
     * Bearer eyJhbGciOi...
     */
    val authToken: String? = null,

    /**
     * Optional HTTP headers supplied by
     * [SyncHeaderProvider].
     *
     * Example:
     *
     * mapOf(
     *     "X-App-Version" to "1.2.0",
     *     "X-Device" to "Android"
     * )
     */
    val headers: Map<String, String> = emptyMap(),

    /**
     * Current retry attempt.
     *
     * Starts at zero and increases whenever synchronization fails.
     */
    val retryCount: Int = 0,

    /**
     * Time when the item entered the offline queue.
     *
     * Unix timestamp in milliseconds.
     */
    val queuedAt: Long,

    /**
     * Last modification timestamp.
     *
     * Unix timestamp in milliseconds.
     */
    val updatedAt: Long,

    /**
     * Additional custom metadata.
     *
     * This field is intentionally generic so future SDK versions
     * can include extra information without breaking the public API.
     */
    val metadata: Map<String, String> = emptyMap()
)