package com.rahuldharmkar.offlinesynckit

import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncRequestTest {

    @Test
    fun `should create sync request correctly`() {

        val request = SyncRequest(
            entityName = "customer",
            entityId = "100",
            operation = SyncOperation.CREATE,
            payload = """{"name":"Rahul"}""",
            authToken = "Bearer token",
            headers = mapOf(
                "Version" to "1.2.0"
            ),
            retryCount = 2,
            queuedAt = 1000L,
            updatedAt = 2000L,
            metadata = mapOf(
                "source" to "sample-app"
            )
        )

        assertEquals("customer", request.entityName)
        assertEquals("100", request.entityId)
        assertEquals(SyncOperation.CREATE, request.operation)
        assertEquals("""{"name":"Rahul"}""", request.payload)
        assertEquals("Bearer token", request.authToken)
        assertEquals("1.2.0", request.headers["Version"])
        assertEquals(2, request.retryCount)
        assertEquals(1000L, request.queuedAt)
        assertEquals(2000L, request.updatedAt)
        assertEquals("sample-app", request.metadata["source"])
    }
}