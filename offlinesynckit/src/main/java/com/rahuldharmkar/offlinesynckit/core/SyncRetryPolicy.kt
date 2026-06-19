package com.rahuldharmkar.offlinesynckit.core

data class SyncRetryPolicy(
    val maxRetryCount: Int = 3
) {
    init {
        require(maxRetryCount >= 0) {
            "maxRetryCount must be greater than or equal to 0"
        }
    }
}