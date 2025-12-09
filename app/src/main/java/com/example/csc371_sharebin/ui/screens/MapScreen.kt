package com.example.csc371_sharebin.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.csc371_sharebin.data.local.BinEntity
import com.example.csc371_sharebin.data.local.BinStatus
import com.example.csc371_sharebin.ui.BinViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.csc371_sharebin.R
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults



/**
 * Main map page showing all donation bins on an interactive OpenStreetMap
 * (osmdroid) map. Handles selecting a bin, zooming to it, and swapping
 * between the carousel and the detailed panel.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: BinViewModel,
    onBack: () -> Unit
) {
    val bins by viewModel.bins.collectAsState()
    var selectedBin by remember { mutableStateOf<BinEntity?>(null) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {

        Configuration.getInstance().userAgentValue = context.packageName
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )

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
                // MAP AREA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    ShareBinMap(
                        bins = bins,
                        focusedBin = selectedBin,
                        onBinSelected = { selectedBin = it }
                    )
                }

                // BOTTOM AREA
                if (selectedBin == null) {
                    BinCarousel(
                        bins = bins,
                        onSelect = { selectedBin = it }
                    )
                } else {
                    BinDetailsPanel(
                        bin = selectedBin!!,
                        onBackToCarousel = { selectedBin = null },
                        onFavoriteToggle = { viewModel.toggleFavorite(selectedBin!!) },
                        onStillHere = { viewModel.markBinVerified(selectedBin!!) },
                        onMissing = { viewModel.markBinMissing(selectedBin!!) }
                    )
                }
            }
        }
    }
}


/**
 * Embeds an osmdroid MapView inside Compose using AndroidView.
 * Displays one marker per bin and recenters the camera when a bin
 * is selected in the carousel or detail view.
 *
 */

@SuppressLint("ClickableViewAccessibility")
    @Composable
    private fun ShareBinMap(
        bins: List<BinEntity>,
        focusedBin: BinEntity?,
        onBinSelected: (BinEntity) -> Unit
    ) {
        val context = LocalContext.current

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx: Context ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    // Initial camera position – Farmingdale State College
                    val startPoint = GeoPoint(40.7537775, -73.4320606)
                    controller.setZoom(16.0)
                    controller.setCenter(startPoint)
                }
            },
            update = { mapView ->
                // Clear old markers
                mapView.overlays.clear()

                // markers for each bin
                bins.forEach { bin ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(bin.latitude, bin.longitude)
                        title = bin.name
                        snippet = bin.operator ?: ""

                        // CUSTOM PIN ICON
                        icon = androidx.core.content.ContextCompat.getDrawable(
                            context,
                            R.drawable.a_pin
                        )

                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)


                    setOnMarkerClickListener { _, _ ->
                            onBinSelected(bin)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }


                focusedBin?.let { fb ->
                    val target = GeoPoint(fb.latitude, fb.longitude)
                    mapView.controller.setZoom(16.0)     // optional: keep map close
                    mapView.controller.animateTo(target) // smooth pan to bin
                }

                // Refresh map
                mapView.invalidate()
            }
        )
    }


/**
 * Bottom horizontal scroll strip showing favorite bins or nearby bins.
 * Tapping a card selects that bin and opens the detail panel.
 *
 */

@Composable
private fun BinCarousel(
    bins: List<BinEntity>,
    onSelect: (BinEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xAAFFFFFF))
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Favorites / Nearby bins",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        val favoriteBins = bins.filter { it.isFavorite }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (favoriteBins.isEmpty()) {
                Text(
                    text = "No favorite bins yet. Tap the heart on a bin to add it here.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                favoriteBins.forEach { bin ->
                    Surface(
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { onSelect(bin) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = bin.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (!bin.operator.isNullOrBlank()) {
                                Text(
                                    text = bin.operator,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                                BinStatusChip(
                                    status = bin.status,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                        }
                    }
                }
            }
        }
    }
}



/**
 * Expanded details panel for a single selected bin, including image,
 * status, accepted item types, and verification actions.
 */

@Composable
private fun BinDetailsPanel(
    bin: BinEntity,
    onBackToCarousel: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onStillHere: () -> Unit,
    onMissing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xDDFFFFFF))
            .padding(12.dp)
    ) {
        BinPhoto(
            bin = bin,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Bin details", style = MaterialTheme.typography.titleSmall)
            Row {
                TextButton(onClick = onBackToCarousel) {
                    Text("Back")
                }
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (bin.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite"
                    )
                }
            }
        }

        OutlinedTextField(
            value = bin.name,
            onValueChange = {},
            label = { Text("Location Name") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        OutlinedTextField(
            value = bin.operator ?: "",
            onValueChange = {},
            label = { Text("Company") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Text(
            text = "Status",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        BinStatusChip(
            status = bin.status,
            modifier = Modifier.padding(top = 4.dp)
        )


        OutlinedTextField(
            value = bin.lastVerifiedAt?.let { "Last seen: $it" } ?: "Last seen: unknown",
            onValueChange = {},
            label = { Text("Last Seen") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Accepted Items", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LabeledCheck("Shoes", bin.acceptedShoes)
            LabeledCheck("Clothing", bin.acceptedClothing)
            LabeledCheck("Electronics", bin.acceptedElectronics)
            LabeledCheck("Other", bin.acceptedOther)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onStillHere) {
                Text("Still here")
            }
            OutlinedButton(onClick = onMissing) {
                Text("Missing")
            }
        }
    }
}

/**
 * Small disabled checkbox paired with a label, used for showing which
 * item types a bin accepts.
 */

@Composable
private fun LabeledCheck(label: String, checked: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {},
            enabled = false
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Colored status indicator for a bin, showing Verified, Missing,
 * or Unverified with themed chip colors.
 *
 */

@Composable
fun BinStatusChip(
    status: BinStatus,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (status) {
        BinStatus.VERIFIED   -> "Verified"   to Color(0xFF4CAF50) // green
        BinStatus.MISSING    -> "Missing"    to Color(0xFFF44336) // red
        BinStatus.UNVERIFIED -> "Unverified" to Color(0xFFFFC107) // amber
    }

    AssistChip(
        onClick = { /* no-op */ },
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        ),
        modifier = modifier
    )
}
