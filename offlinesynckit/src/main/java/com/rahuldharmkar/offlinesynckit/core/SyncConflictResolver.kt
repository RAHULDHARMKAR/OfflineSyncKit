package com.rahuldharmkar.offlinesynckit.core

fun interface SyncConflictResolver {

    suspend fun resolve(conflict: SyncConflict): SyncConflictResolution
}