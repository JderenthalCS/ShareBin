package com.example.csc371_sharebin.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a donation bin stored in the local Room database.
 * Holds location info, accepted items, photos, favorite status,
 * and verification tracking used throughout the ShareBin app.
 */
@Entity(tableName = "bins")
data class BinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    // Basic Bin-fo
    val name: String,
    val operator: String?,

    // Location Lat x Long
    val latitude: Double,
    val longitude: Double,

    // Photo Location
    val photoUri: String?,

    // Accepted Filters
    val acceptedClothing: Boolean,
    val acceptedShoes: Boolean,
    val acceptedElectronics: Boolean,
    val acceptedOther: Boolean,
    val isFavorite: Boolean = false,

    // Verification
    val lastVerifiedAt: Long? = null,
    val verificationCount: Int = 0,
    val status: BinStatus = BinStatus.UNVERIFIED
)
