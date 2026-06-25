package com.rahuldharmkar.offlinesynckit



import com.rahuldharmkar.offlinesynckit.core.SyncSerializer
import com.rahuldharmkar.offlinesynckit.core.SyncSerializerRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SyncSerializerRegistryTest {

    data class Customer(
        val id: String,
        val name: String
    )

    data class Order(
        val id: String,
        val amount: Double
    )

    @Test
    fun `registered serializer should be returned for same type`() {
        val registry = SyncSerializerRegistry()

        val serializer = SyncSerializer<Customer> { customer ->
            """{"id":"${customer.id}","name":"${customer.name}"}"""
        }

        registry.register(Customer::class, serializer)

        val result = registry.get(Customer::class)

        assertNotNull(result)
    }

    @Test
    fun `registered serializer should serialize object correctly`() {
        val registry = SyncSerializerRegistry()

        registry.register(
            Customer::class,
            SyncSerializer<Customer> { customer ->
                """{"id":"${customer.id}","name":"${customer.name}"}"""
            }
        )

        val customer = Customer(
            id = "1",
            name = "Rahul"
        )

        val serializer = registry.get(Customer::class)

        val json = serializer?.serialize(customer)

        assertEquals("""{"id":"1","name":"Rahul"}""", json)
    }

    @Test
    fun `unregistered type should return null`() {
        val registry = SyncSerializerRegistry()

        val serializer = registry.get(Order::class)

        assertNull(serializer)
    }
}