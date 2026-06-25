package com.rahuldharmkar.offlinesynckit.internal.data.local

import androidx.room.TypeConverter
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncStatus

internal class SyncConverters {

    @TypeConverter
    fun fromOperation(value: SyncOperation): String = value.name

    @TypeConverter
    fun toOperation(value: String): SyncOperation = com.rahuldharmkar.offlinesynckit.core.SyncOperation.valueOf(value)

    @TypeConverter
    fun fromStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): SyncStatus = com.rahuldharmkar.offlinesynckit.core.SyncStatus.valueOf(value)
}