package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.core.SyncSerializer
import com.rahuldharmkar.offlinesynckit.core.SyncSerializerRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncSerializerTypeApiTest {

    data class Customer(
        val id: String,
        val name: String
    )

    @Test
    fun `registry should return serializer using explicit type`() {
        val registry = SyncSerializerRegistry()

        registry.register(
            Customer::class,
            SyncSerializer<Customer> { customer ->
                """{"id":"${customer.id}","name":"${customer.name}"}"""
            }
        )

        val serializer = registry.get(Customer::class)

        val result = serializer?.serialize(
            Customer(
                id = "101",
                name = "Rahul"
            )
        )

        assertEquals("""{"id":"101","name":"Rahul"}""", result)
    }
}