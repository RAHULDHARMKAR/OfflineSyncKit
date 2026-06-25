package com.rahuldharmkar.offlinesynckit.core

fun interface SyncSerializer<T> {
    fun serialize(value: T): String
}