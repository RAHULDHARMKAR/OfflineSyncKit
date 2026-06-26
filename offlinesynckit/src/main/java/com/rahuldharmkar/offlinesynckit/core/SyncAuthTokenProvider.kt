package com.rahuldharmkar.offlinesynckit.core

fun interface SyncAuthTokenProvider {
    suspend fun getToken(): String?
}