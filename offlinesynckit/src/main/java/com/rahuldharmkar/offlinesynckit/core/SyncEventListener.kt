package com.rahuldharmkar.offlinesynckit.core

fun interface SyncEventListener {
    fun onEvent(event: SyncEvent)
}