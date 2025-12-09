package com.example.csc371_sharebin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Main Room database for ShareBin.
 * Stores all donation bin records and provides access to the BinDao.
 * Uses type converters for custom types like BinStatus.
 */
@Database(
    entities = [BinEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ShareBinTypeConverters::class)
abstract class ShareBinDatabase : RoomDatabase() {
    abstract fun binDao(): BinDao
}
