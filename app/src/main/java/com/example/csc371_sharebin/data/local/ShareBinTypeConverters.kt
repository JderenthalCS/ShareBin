package com.example.csc371_sharebin.data.local

import androidx.room.TypeConverter

/**
 * Stores TypeConverters as ROOM cannot store enums directly
 */
class ShareBinTypeConverters {

    @TypeConverter
    fun fromStatus(status: BinStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): BinStatus = BinStatus.valueOf(value)
}
