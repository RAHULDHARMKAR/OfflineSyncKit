package com.rahuldharmkar.offlinesynckit.internal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity): Long

    @Query("""
        SELECT * FROM sync_queue 
        WHERE status IN (:statuses)
        ORDER BY createdAt ASC
        LIMIT :limit
    """)
    suspend fun getPendingItems(
        statuses: List<com.rahuldharmkar.offlinesynckit.core.SyncStatus> = listOf(com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING, com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED),
        limit: Int = 20
    ): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SyncQueueEntity>>

    @Query("UPDATE sync_queue SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(
        id: Long,
        status: com.rahuldharmkar.offlinesynckit.core.SyncStatus,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
    UPDATE sync_queue 
    SET status = :status,
        retryCount = retryCount + 1,
        lastError = :error,
        updatedAt = :updatedAt 
    WHERE id = :id
""")
    suspend fun markFailed(
        id: Long,
        status: com.rahuldharmkar.offlinesynckit.core.SyncStatus,
        error: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sync_queue WHERE status = :status")
    suspend fun deleteByStatus(status: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED)

    @Query("""
    SELECT * FROM sync_queue 
    WHERE status = :status
    ORDER BY createdAt ASC
""")
    suspend fun getItemsByStatus(
        status: com.rahuldharmkar.offlinesynckit.core.SyncStatus
    ): List<SyncQueueEntity>

    @Query("""
    UPDATE sync_queue 
    SET status = :status,
        retryCount = 0,
        lastError = NULL,
        updatedAt = :updatedAt
    WHERE id = :id
""")
    suspend fun resetForRetry(
        id: Long,
        status: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
    UPDATE sync_queue 
    SET status = :newStatus,
        retryCount = 0,
        lastError = NULL,
        updatedAt = :updatedAt
    WHERE status = :oldStatus
""")
    suspend fun resetAllByStatus(
        oldStatus: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.GIVE_UP,
        newStatus: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING,
        updatedAt: Long = System.currentTimeMillis()
    )


    @Query("""
    DELETE FROM sync_queue
    WHERE entityName = :entityName
      AND entityId = :entityId
      AND operation = :operation
      AND status IN (:statuses)
""")
    suspend fun deletePendingSameEntityOperation(
        entityName: String,
        entityId: String,
        operation: com.rahuldharmkar.offlinesynckit.core.SyncOperation,
        statuses: List<com.rahuldharmkar.offlinesynckit.core.SyncStatus> = listOf(com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING, com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED)
    )

    @Query("""
    DELETE FROM sync_queue
    WHERE entityName = :entityName
      AND entityId = :entityId
      AND status IN (:statuses)
""")
    suspend fun deletePendingSameEntity(
        entityName: String,
        entityId: String,
        statuses: List<com.rahuldharmkar.offlinesynckit.core.SyncStatus> = listOf(com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING, com.rahuldharmkar.offlinesynckit.core.SyncStatus.FAILED)
    )

    @Query("""
    UPDATE sync_queue
    SET status = :newStatus,
        updatedAt = :updatedAt
    WHERE status = :oldStatus
      AND updatedAt < :staleBefore
""")
    suspend fun resetStaleSyncingItems(
        oldStatus: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCING,
        newStatus: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING,
        staleBefore: Long,
        updatedAt: Long = System.currentTimeMillis()
    )



    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()

    @Query("""
    DELETE FROM sync_queue
    WHERE status = :status
      AND updatedAt < :olderThan
""")
    suspend fun deleteSyncedOlderThan(
        status: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.SYNCED,
        olderThan: Long
    )

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = :status")
    suspend fun countByStatus(status: com.rahuldharmkar.offlinesynckit.core.SyncStatus): Int

    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun countAll(): Int

    @Query("SELECT * FROM sync_queue")
    fun observeForStats(): Flow<List<SyncQueueEntity>>

    @Query("""
    UPDATE sync_queue
    SET payload = :payload,
        status = :status,
        retryCount = 0,
        lastError = NULL,
        updatedAt = :updatedAt
    WHERE id = :id
""")
    suspend fun updatePayloadAndStatus(
        id: Long,
        payload: String,
        status: com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.PENDING,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getAllItems(): List<SyncQueueEntity>


    @Query("""
    SELECT * FROM sync_queue
    WHERE (:status IS NULL OR status = :status)
      AND (:entityName IS NULL OR entityName = :entityName)
      AND (:operation IS NULL OR operation = :operation)
    ORDER BY createdAt ASC
    LIMIT :limit
""")
    suspend fun queryQueue(
        status: SyncStatus?,
        entityName: String?,
        operation: SyncOperation?,
        limit: Int
    ): List<SyncQueueEntity>


}