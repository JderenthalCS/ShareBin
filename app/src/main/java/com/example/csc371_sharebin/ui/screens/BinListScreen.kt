package com.example.csc371_sharebin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.csc371_sharebin.data.local.BinEntity
import com.example.csc371_sharebin.data.local.BinStatus
import com.example.csc371_sharebin.ui.BinViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.csc371_sharebin.R
import androidx.compose.ui.res.painterResource

/**
 * Main screen showing the full list of donation bins.
 * Supports searching, filtering, favorites, verifying, and navigating
 * to the Add Bin screen, Map screen, or logging out.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinListScreen(
    viewModel: BinViewModel,
    onAddBinClick: () -> Unit,
    onMapClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val bins by viewModel.bins.collectAsState()


    // Filter state
    var showVerifiedOnly by remember { mutableStateOf(false) }
    var filterClothing by remember { mutableStateOf(false) }
    var filterShoes by remember { mutableStateOf(false) }
    var filterElectronics by remember { mutableStateOf(false) }
    var filterOther by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }


    // Bin selected for verification dialog
    var binToVerify by remember { mutableStateOf<BinEntity?>(null) }

    // Apply filters to bins list
    val filteredBins = bins
        .filter { bin ->
            val passVerified =
                if (showVerifiedOnly) bin.status == BinStatus.VERIFIED else true

            val anyFilterOn =
                filterClothing || filterShoes || filterElectronics || filterOther

            val passItems =
                if (!anyFilterOn) {
                    true
                } else {
                    (filterClothing && bin.acceptedClothing) ||
                            (filterShoes && bin.acceptedShoes) ||
                            (filterElectronics && bin.acceptedElectronics) ||
                            (filterOther && bin.acceptedOther)
                }

            passVerified && passItems
        }
        .filter { bin ->
            val q = searchQuery.trim().lowercase()
            if (q.isBlank()) true
            else {
                bin.name.lowercase().contains(q) ||
                        (bin.operator ?: "").lowercase().contains(q)
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.a_title),
                        contentDescription = "ShareBin Logo",
                        modifier = Modifier.height(60.dp),
                        contentScale = ContentScale.FillHeight
                    )
                },
                actions = {
                    IconButton(onClick = onMapClick) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "Map"
                        )
                    }

                    Text(
                        text = "Logout",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onLogoutClick() }
                    )
                }
            )

        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBinClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add bin"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.a_base),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Foreground content
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    singleLine = true,
                    placeholder = { Text("Search bins by name or company") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                )

                // Filters
                FilterBar(
                    showVerifiedOnly = showVerifiedOnly,
                    onVerifiedOnlyChange = { showVerifiedOnly = it },
                    filterClothing = filterClothing,
                    onClothingChange = { filterClothing = it },
                    filterShoes = filterShoes,
                    onShoesChange = { filterShoes = it },
                    filterElectronics = filterElectronics,
                    onElectronicsChange = { filterElectronics = it },
                    filterOther = filterOther,
                    onOtherChange = { filterOther = it }
                )
                BinStatsCard(bins = bins)
                // List of bins
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredBins) { bin ->
                        BinCard(
                            bin = bin,
                            onFavoriteClick = { viewModel.toggleFavorite(bin) },
                            onVerifyClick = { binToVerify = bin }
                        )
                    }
                }
            }
        }
    }


    // Verification dialog
    if (binToVerify != null) {
        VerifyBinDialog(
            bin = binToVerify!!,
            onDismiss = { binToVerify = null },
            onStillHere = {
                viewModel.markBinVerified(binToVerify!!)
                binToVerify = null
            },
            onMissing = {
                viewModel.markBinMissing(binToVerify!!)
                binToVerify = null
            }
        )
    }
}

/**
 * Displays filter controls for narrowing down the list of bins.
 * Includes verified-only checkbox and category chips such as clothing,
 * shoes, electronics, and other.
 */
@Composable
private fun FilterBar(
    showVerifiedOnly: Boolean,
    onVerifiedOnlyChange: (Boolean) -> Unit,
    filterClothing: Boolean,
    onClothingChange: (Boolean) -> Unit,
    filterShoes: Boolean,
    onShoesChange: (Boolean) -> Unit,
    filterElectronics: Boolean,
    onElectronicsChange: (Boolean) -> Unit,
    filterOther: Boolean,
    onOtherChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showVerifiedOnly,
                onCheckedChange = onVerifiedOnlyChange
            )
            Text(text = "Verified only")
        }

        Spacer(modifier = Modifier.width(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "Clothing",
                selected = filterClothing,
                onClick = { onClothingChange(!filterClothing) }
            )
            FilterChip(
                label = "Shoes",
                selected = filterShoes,
                onClick = { onShoesChange(!filterShoes) }
            )
            FilterChip(
                label = "Electronics",
                selected = filterElectronics,
                onClick = { onElectronicsChange(!filterElectronics) }
            )
            FilterChip(
                label = "Other",
                selected = filterOther,
                onClick = { onOtherChange(!filterOther) }
            )
        }
    }
}

/**
 * Reusable chip component used for toggling a single filter option.
 */
@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Visual card displaying a single donation bin.
 * Shows name, operator, status, category chips, and favorite toggle.
 * Clicking the card opens the verification dialog.
 */

@Composable
private fun BinCard(
    bin: BinEntity,
    onFavoriteClick: () -> Unit,
    onVerifyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onVerifyClick() },
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            BinPhoto(bin = bin)
        }
            Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bin.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (!bin.operator.isNullOrBlank()) {
                        Text(
                            text = bin.operator,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    BinStatusChip(
                        status = bin.status,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = if (bin.isFavorite) "Favorite" else "Tap heart to favorite",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (bin.isFavorite)
                            Icons.Filled.Favorite
                        else
                            Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (bin.isFavorite)
                            Color(0xFF4CAF50)   // ShareBin green
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Status: ${bin.status.name} · Verified ${bin.verificationCount}x",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (bin.acceptedClothing) ItemChip("Clothing")
                if (bin.acceptedShoes) ItemChip("Shoes")
                if (bin.acceptedElectronics) ItemChip("Electronics")
                if (bin.acceptedOther) ItemChip("Other")
            }
        }
    }
}

/**
 * Small colored chip used to display accepted item types on a bin card.
 */

@Composable
private fun ItemChip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = Color(0xFFBBDEFB),           // light blue chip bg
        tonalElevation = 0.dp
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Dialog prompting the user to verify whether a bin is still at its location.
 *
 */

@Composable
private fun VerifyBinDialog(
    bin: BinEntity,
    onDismiss: () -> Unit,
    onStillHere: () -> Unit,
    onMissing: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Verify bin")
        },
        text = {
            Text(text = "Is \"${bin.name}\" still at this location?")
        },
        confirmButton = {
            TextButton(onClick = onStillHere) {
                Text("Still here")
            }
        },
        dismissButton = {
            TextButton(onClick = onMissing) {
                Text("Missing")
            }
        }
    )
}

/**
 * Displays simple statistics about the current bin list,
 * such as total count, verified vs. missing, and favorite count.
 *
 */

@Composable
private fun BinStatsCard(
    bins: List<BinEntity>,
    modifier: Modifier = Modifier
) {
    if (bins.isEmpty()) return

    val total = bins.size
    val verified = bins.count { it.status == BinStatus.VERIFIED }
    val missing = bins.count { it.status == BinStatus.MISSING }
    val unverified = bins.count { it.status == BinStatus.UNVERIFIED }
    val favorites = bins.count { it.isFavorite }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "ShareBin Statistics:",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total bins: $total  •  Favorites: $favorites",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Verified: $verified   Missing: $missing   Unverified: $unverified",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
