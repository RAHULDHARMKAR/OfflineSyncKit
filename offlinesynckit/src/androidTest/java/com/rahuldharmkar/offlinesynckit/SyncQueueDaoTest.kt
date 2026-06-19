package com.rahuldharmkar.offlinesynckit

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncDatabase
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueDao
import com.rahuldharmkar.offlinesynckit.internal.data.local.SyncQueueEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncQueueDaoTest {

    private lateinit var database: SyncDatabase
    private lateinit var dao: SyncQueueDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            SyncDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = database.syncQueueDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertQueueItem_shouldSaveItem() = runTest {
        val id = dao.insert(createEntity())

        val items = dao.getPendingItems()

        assertEquals(id, items.first().id)
        assertEquals("customer", items.first().entityName)
        assertEquals(com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING, items.first().status)
    }

    @Test
    fun updateStatus_shouldChangeItemStatus() = runTest {
        val id = dao.insert(createEntity())

        dao.updateStatus(id, com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCING)

        val syncingCount = dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCING)

        assertEquals(1, syncingCount)
    }

    @Test
    fun markFailed_shouldIncrementRetryCount() = runTest {
        val id = dao.insert(createEntity())

        dao.markFailed(
            id = id,
            status = com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED,
            error = "Network error"
        )

        val items = dao.getPendingItems()

        assertEquals(com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED, items.first().status)
        assertEquals(1, items.first().retryCount)
        assertEquals("Network error", items.first().lastError)
    }

    @Test
    fun resetAllByStatus_shouldMoveGiveUpToPending() = runTest {
        val id = dao.insert(createEntity())

        dao.markFailed(
            id = id,
            status = com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP,
            error = "Max retry reached"
        )

        dao.resetAllByStatus(
            oldStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP,
            newStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING
        )

        val items = dao.getPendingItems()

        assertEquals(1, items.size)
        assertEquals(com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING, items.first().status)
        assertEquals(0, items.first().retryCount)
    }

    @Test
    fun deleteSyncedOlderThan_shouldDeleteOldSyncedItems() = runTest {
        val oldTime = System.currentTimeMillis() - 120_000

        val id = dao.insert(
            createEntity(
                status = com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED,
                updatedAt = oldTime
            )
        )

        dao.deleteSyncedOlderThan(
            olderThan = System.currentTimeMillis()
        )

        assertEquals(0, dao.countAll())
    }

    @Test
    fun countByStatus_shouldReturnCorrectCount() = runTest {
        dao.insert(createEntity(status = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING))
        dao.insert(createEntity(status = com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED))
        dao.insert(createEntity(status = com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED))

        assertEquals(1, dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING))
        assertEquals(1, dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED))
        assertEquals(1, dao.countByStatus(com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED))
        assertEquals(3, dao.countAll())
    }

    private fun createEntity(
        entityName: String = "customer",
        entityId: String = "123",
        operation: com.rahuldharmkar.offlinesynckit.core.SyncOperation = com.rahuldharmkar.offlinesynckit.core.SyncOperation.CREATE,
        payload: String = """{"name":"Rahul"}""",
        status: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING,
        updatedAt: Long = System.currentTimeMillis()
    ): SyncQueueEntity {
        return SyncQueueEntity(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            payload = payload,
            status = status,
            updatedAt = updatedAt
        )
    }
}