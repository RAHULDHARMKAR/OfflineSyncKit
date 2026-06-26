package com.rahuldharmkar.offlinesynckit.core

fun interface SyncHeaderProvider {
    suspend fun getHeaders(): Map<String, String>
}