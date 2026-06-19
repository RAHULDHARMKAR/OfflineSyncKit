package com.rahuldharmkar.offlinesynckit.internal

internal object SyncValidator {

    fun validateEnqueueInput(
        entityName: String,
        entityId: String,
        payload: String
    ) {
        require(entityName.isNotBlank()) {
            "entityName must not be blank"
        }

        require(entityId.isNotBlank()) {
            "entityId must not be blank"
        }

        require(payload.isNotBlank()) {
            "payload must not be blank"
        }
    }

    fun validateSyncLimit(limit: Int) {
        require(limit > 0) {
            "sync limit must be greater than 0"
        }
    }
}