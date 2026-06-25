package com.rahuldharmkar.offlinesynckit.core

import kotlin.reflect.KClass

class SyncSerializerRegistry {

    private val serializers = mutableMapOf<KClass<*>, SyncSerializer<*>>()

    fun <T : Any> register(
        type: KClass<T>,
        serializer: SyncSerializer<T>
    ) {
        serializers[type] = serializer
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(
        type: KClass<T>
    ): SyncSerializer<T>? {
        return serializers[type] as? SyncSerializer<T>
    }
}