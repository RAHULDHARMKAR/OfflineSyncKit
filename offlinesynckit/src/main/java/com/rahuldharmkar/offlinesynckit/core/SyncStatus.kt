package com.rahuldharmkar.offlinesynckit.core

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED,
    CONFLICT,
    GIVE_UP
}