package com.rahuldharmkar.offlinesynckit.internal.data.local

import androidx.room.TypeConverter
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncStatus

internal class SyncConverters {

    @TypeConverter
    fun fromOperation(value: com.rahuldharmkar.offlinesynckit.core.SyncOperation): String = value.name

    @TypeConverter
    fun toOperation(value: String): com.rahuldharmkar.offlinesynckit.core.SyncOperation = com.rahuldharmkar.offlinesynckit.core.SyncOperation.valueOf(value)

    @TypeConverter
    fun fromStatus(value: com.rahuldharmkar.offlinesynckit.core.SyncStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): com.rahuldharmkar.offlinesynckit.core.SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.valueOf(value)
}