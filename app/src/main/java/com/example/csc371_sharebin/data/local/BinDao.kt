package com.example.csc371_sharebin.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and modifying donation bin data stored in Room.
 * Provides queries for listing bins, updating favorites, and verifying bin status.
 */
@Dao
interface BinDao {

    // Get All Bins
    @Query("SELECT * FROM bins ORDER BY name ASC")
    fun getAllBins(): Flow<List<BinEntity>>

    // Insert Bin
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBin(bin: BinEntity)

    // Update Favorites
    @Query("UPDATE bins SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    // Get Bin Count
    @Query("SELECT COUNT(*) FROM bins")
    suspend fun getBinCount(): Int

    // Missing / Still - update verification info
    @Query("""
        UPDATE bins
        SET status = :status,
            lastVerifiedAt = :lastVerifiedAt,
            verificationCount = verificationCount + 1
        WHERE id = :id
    """)
    suspend fun updateVerification(
        id: Long,
        status: BinStatus,
        lastVerifiedAt: Long
    )
}



