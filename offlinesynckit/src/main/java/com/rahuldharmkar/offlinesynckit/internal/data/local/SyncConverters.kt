package com.rahuldharmkar.offlinesynckit.internal.data.local

import androidx.room.TypeConverter
import com.rahuldharmkar.offlinesynckit.core.SyncOperation
import com.rahuldharmkar.offlinesynckit.core.SyncStatus

class SyncConverters {

    @TypeConverter
    fun fromOperation(value: SyncOperation?): String? {
        return value?.name
    }

    @TypeConverter
    fun toOperation(value: String?): SyncOperation? {
        return value?.let {
            SyncOperation.valueOf(it)
        }
    }

    @TypeConverter
    fun fromStatus(value: SyncStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toStatus(value: String?): SyncStatus? {
        return value?.let {
            SyncStatus.valueOf(it)
        }
    }
}