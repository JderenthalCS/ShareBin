package com.example.csc371_sharebin.data

import com.example.csc371_sharebin.data.local.BinDao
import com.example.csc371_sharebin.data.local.BinEntity
import com.example.csc371_sharebin.data.local.BinStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository layer for all bin-related data operations.
 * Acts as a simple wrapper around the DAO so the rest of the app
 * doesn’t talk to the database directly.
 */
class BinRepository(
    private val binDao: BinDao
) {

    // Returns a Flow stream of all bins in the database.
    fun getAllBins(): Flow<List<BinEntity>> = binDao.getAllBins()

    // Inserts a new bin or updates it if it already exists.
    suspend fun addBin(bin: BinEntity) {
        binDao.insertBin(bin)
    }

    // Updates whether a bin is marked as a favorite.
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        binDao.updateFavorite(id, isFavorite)
    }

    // Updates a bin’s verification status and timestamp.
    suspend fun verifyBin(id: Long, status: BinStatus, timestamp: Long) {
        binDao.updateVerification(id, status, timestamp)
    }

    // Returns true if there is at least one bin in the database.
    suspend fun hasAnyBins(): Boolean = binDao.getBinCount() > 0
}

