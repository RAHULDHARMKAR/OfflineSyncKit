package com.rahuldharmkar.offlinesynckit


import com.rahuldharmkar.offlinesynckit.internal.SyncValidator
import org.junit.Test

class SyncValidatorTest {

    @Test
    fun `valid enqueue input should not throw exception`() {
        SyncValidator.validateEnqueueInput(
            entityName = "customer",
            entityId = "123",
            payload = """{"name":"Rahul"}"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank entity name should throw exception`() {
        SyncValidator.validateEnqueueInput(
            entityName = "",
            entityId = "123",
            payload = """{"name":"Rahul"}"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank entity id should throw exception`() {
        SyncValidator.validateEnqueueInput(
            entityName = "customer",
            entityId = "",
            payload = """{"name":"Rahul"}"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank payload should throw exception`() {
        SyncValidator.validateEnqueueInput(
            entityName = "customer",
            entityId = "123",
            payload = ""
        )
    }

    @Test
    fun `valid sync limit should not throw exception`() {
        SyncValidator.validateSyncLimit(20)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero sync limit should throw exception`() {
        SyncValidator.validateSyncLimit(0)
    }
}