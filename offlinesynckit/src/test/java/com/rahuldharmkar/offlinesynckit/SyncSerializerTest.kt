package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.core.SyncSerializer
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncSerializerTest {

    data class Customer(
        val id: String,
        val name: String
    )

    @Test
    fun `serializer should convert object to string`() {
        val serializer = SyncSerializer<Customer> { customer ->
            """{"id":"${customer.id}","name":"${customer.name}"}"""
        }

        val result = serializer.serialize(
            Customer(
                id = "1",
                name = "Rahul"
            )
        )

        assertEquals("""{"id":"1","name":"Rahul"}""", result)
    }
}