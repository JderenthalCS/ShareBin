package com.example.csc371_sharebin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csc371_sharebin.data.BinRepository
import com.example.csc371_sharebin.data.local.BinEntity
import com.example.csc371_sharebin.data.local.BinStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for all ShareBin app logic. Holds the current list of bins,
 * handles adding, updating, verifying, and seeding demo data.
 */
class BinViewModel(
    private val repository: BinRepository
) : ViewModel() {

    val bins: StateFlow<List<BinEntity>> =
        repository.getAllBins()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addSampleBinIfEmpty() {
        viewModelScope.launch {
            // If we already have bins, don't seed demo data
            if (repository.hasAnyBins()) return@launch

            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            val demoBins = listOf(
                BinEntity(
                    name = "Campus Clothing Bin – Lot 1",
                    operator = "FSC Sustainability",
                    latitude = 40.7539,
                    longitude = -73.4322,
                    photoUri = "b1",
                    acceptedClothing = true,
                    acceptedShoes = true,
                    acceptedElectronics = false,
                    acceptedOther = false,
                    isFavorite = true,
                    lastVerifiedAt = now - 3 * dayMs,
                    verificationCount = 3,
                    status = BinStatus.VERIFIED
                ),
                BinEntity(
                    name = "Westbury Thrift Spot - Parking East",
                    operator = "Island Relief Collective",
                    latitude = 40.7680,
                    longitude = -73.5855,
                    photoUri = "b2",
                    acceptedClothing = true,
                    acceptedShoes = true, // Changed from false
                    acceptedElectronics = false,
                    acceptedOther = true,
                    isFavorite = false,
                    lastVerifiedAt = now - 7 * dayMs,
                    verificationCount = 2,
                    status = BinStatus.VERIFIED
                ),
                BinEntity(
                    name = "Huntington Station Recycle Point",
                    operator = "Suffolk Green Initiative",
                    latitude = 40.8360,
                    longitude = -73.4150,
                    photoUri = "b3",
                    acceptedClothing = true, // Changed from false
                    acceptedShoes = false,
                    acceptedElectronics = true,
                    acceptedOther = false, // Changed from true
                    isFavorite = true,
                    lastVerifiedAt = now - 1 * dayMs,
                    verificationCount = 4,
                    status = BinStatus.VERIFIED
                ),
                BinEntity(
                    name = "Farmingdale E-Waste Drop-Off",
                    operator = "Tech-Cycle LI",
                    latitude = 40.7305,
                    longitude = -73.4500,
                    photoUri = "b4",
                    acceptedClothing = false,
                    acceptedShoes = false,
                    acceptedElectronics = true,
                    acceptedOther = false,
                    isFavorite = false,
                    lastVerifiedAt = now - 10 * dayMs,
                    verificationCount = 1,
                    status = BinStatus.VERIFIED
                ),
                BinEntity(
                    name = "Massapequa Park Shoe Collection",
                    operator = "Stepping Up Foundation",
                    latitude = 40.6690,
                    longitude = -73.4735,
                    photoUri = "b5",
                    acceptedClothing = false,
                    acceptedShoes = true,
                    acceptedElectronics = true, // Changed from false
                    acceptedOther = false,
                    isFavorite = true,
                    lastVerifiedAt = now - 5 * dayMs,
                    verificationCount = 3,
                    status = BinStatus.VERIFIED
                ),
                BinEntity(
                    name = "Riverhead Community Donation Hub",
                    operator = "North Fork Aid",
                    latitude = 40.9160,
                    longitude = -72.6515,
                    photoUri = "b6",
                    acceptedClothing = true,
                    acceptedShoes = true,
                    acceptedElectronics = false, // Changed from true
                    acceptedOther = true,
                    isFavorite = false,
                    lastVerifiedAt = now - 15 * dayMs,
                    verificationCount = 5,
                    status = BinStatus.VERIFIED
                ),
                BinEntity(
                    name = "Patchogue Avenue Clothing Bin",
                    operator = "Coastal Community Outreach",
                    latitude = 40.7710,
                    longitude = -73.0100,
                    photoUri = "b7",
                    acceptedClothing = true,
                    acceptedShoes = false, // Changed from true
                    acceptedElectronics = false,
                    acceptedOther = true, // Changed from false
                    isFavorite = true,
                    lastVerifiedAt = now - 20 * dayMs,
                    verificationCount = 6,
                    status = BinStatus.VERIFIED
                ),
                BinEntity(
                    name = "Freeport Textiles Only Drop-Off",
                    operator = "Nassau Fiber Recovery",
                    latitude = 40.6550,
                    longitude = -73.5850,
                    photoUri = "b8",
                    acceptedClothing = true,
                    acceptedShoes = false,
                    acceptedElectronics = false,
                    acceptedOther = false,
                    isFavorite = false,
                    lastVerifiedAt = now - 30 * dayMs,
                    verificationCount = 2,
                    status = BinStatus.VERIFIED
                ),
                BinEntity(
                    name = "Port Washington Miscellaneous Bin",
                    operator = "Harbor Helpers",
                    latitude = 40.8260,
                    longitude = -73.7050,
                    photoUri = "b9",
                    acceptedClothing = true,
                    acceptedShoes = true,
                    acceptedElectronics = true,
                    acceptedOther = true,
                    isFavorite = false,
                    lastVerifiedAt = null,
                    verificationCount = 0,
                    status = BinStatus.UNVERIFIED
                ),
                BinEntity(
                    name = "Babylon Village Decommissioned Bin",
                    operator = "Retired Bins Co.",
                    latitude = 40.6975,
                    longitude = -73.3270,
                    photoUri = "b10",
                    acceptedClothing = false,
                    acceptedShoes = false,
                    acceptedElectronics = false,
                    acceptedOther = false,
                    isFavorite = false,
                    lastVerifiedAt = now - 60 * dayMs,
                    verificationCount = 1,
                    status = BinStatus.MISSING
                )
            )

            demoBins.forEach { repository.addBin(it) }
        }
    }



    fun toggleFavorite(bin: BinEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(bin.id, !bin.isFavorite)
        }
    }

    fun createBin(
        name: String,
        operator: String?,
        clothing: Boolean,
        shoes: Boolean,
        electronics: Boolean,
        other: Boolean,
        latitude: Double,
        longitude: Double,
        photoUri: String?
    ) {
        viewModelScope.launch {
            val bin = BinEntity(
                name = name,
                operator = operator?.ifBlank { null },
                latitude = latitude,
                longitude = longitude,
                photoUri = photoUri,
                acceptedClothing = clothing,
                acceptedShoes = shoes,
                acceptedElectronics = electronics,
                acceptedOther = other
            )
            repository.addBin(bin)
        }
    }
    fun markBinVerified(bin: BinEntity) {
        viewModelScope.launch {
            repository.verifyBin(
                id = bin.id,
                status = BinStatus.VERIFIED,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun markBinMissing(bin: BinEntity) {
        viewModelScope.launch {
            repository.verifyBin(
                id = bin.id,
                status = BinStatus.MISSING,
                timestamp = System.currentTimeMillis()
            )
        }
    }

}