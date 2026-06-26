package com.rahuldharmkar.offlinesynckit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncApiResult
import com.rahuldharmkar.offlinesynckit.core.SyncConfig
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncRequest
import com.rahuldharmkar.offlinesynckit.core.SyncSerializer
import com.rahuldharmkar.offlinesynckit.core.SyncSerializerRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchEnqueueTest {

    data class Customer(
        val id: String,
        val name: String
    )

    @Test
    fun enqueueObjectsShouldInsertAllItems() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val registry = SyncSerializerRegistry().apply {
            register(
                Customer::class,
                SyncSerializer<Customer> { customer ->
                    """{"id":"${customer.id}","name":"${customer.name}"}"""
                }
            )
        }

        val syncKit = SyncClient.Builder(context)
            .apiAdapter(
                SyncApiAdapter { _: SyncRequest ->
                    SyncApiResult(success = true)
                }
            )
            .config(
                SyncConfig(
                    serializerRegistry = registry,
                    autoSyncWhenOnline = false
                )
            )
            .build()

        val customers = listOf(
            Customer(id = "1", name = "Rahul"),
            Customer(id = "2", name = "Amit"),
            Customer(id = "3", name = "Neha")
        )

        val ids = syncKit.enqueueObjects(
            entityName = "customer",
            items = customers,
            operation = SyncOperation.CREATE,
            type = Customer::class,
            entityIdProvider = { it.id }
        )

        val queueItems = syncKit.observeQueueSnapshot()

        assertEquals(3, ids.size)
        assertEquals(3, queueItems.size)
    }

    @Test
    fun enqueueObjectsShouldFailForEmptyList() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val syncKit = SyncClient.Builder(context)
            .apiAdapter(
                SyncApiAdapter { _: SyncRequest ->
                    SyncApiResult(success = true)
                }
            )
            .build()

        val exception = runCatching {
            syncKit.enqueueObjects(
                entityName = "customer",
                items = emptyList<Customer>(),
                operation = SyncOperation.CREATE,
                serializer = { """{}""" },
                entityIdProvider = { it.id }
            )
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
        assertEquals("items must not be empty", exception?.message)
    }
}