package com.example.csc371_sharebin.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.csc371_sharebin.ui.BinViewModel
import com.google.android.gms.location.LocationServices
import com.example.csc371_sharebin.R
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

/**
 * Main screen for creating a new donation bin.
 * Handles text input, item filters, GPS capture, photo picking,
 * and saving the final bin into the database through the ViewModel.
 */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBinScreen(
    viewModel: BinViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var name by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("") }

    var clothing by remember { mutableStateOf(false) }
    var shoes by remember { mutableStateOf(false) }
    var electronics by remember { mutableStateOf(false) }
    var other by remember { mutableStateOf(false) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val latText = latitude?.toString() ?: "Not set"
    val lonText = longitude?.toString() ?: "Not set"

    // control asking for permission then reading location
    var pendingLocationRequest by remember { mutableStateOf(false) }

    // Photo for this bin (URI string)
    var photoUri by remember { mutableStateOf<String?>(null) }

    // Image picker from gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri?.toString()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingLocationRequest) {
            pendingLocationRequest = false
            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                loc?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            }
        } else {
            pendingLocationRequest = false

        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add A Donation Bin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        enabled = name.isNotBlank() && latitude != null && longitude != null,
                        onClick = {
                            viewModel.createBin(
                                name = name,
                                operator = operator,
                                clothing = clothing,
                                shoes = shoes,
                                electronics = electronics,
                                other = other,
                                latitude = latitude ?: 0.0,
                                longitude = longitude ?: 0.0,
                                photoUri = photoUri
                            )
                            onBack()
                        }
                    ) {
                        Text("Save")
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
            // BACKGROUND
            Image(
                painter = painterResource(id = R.drawable.a_base),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAAFFFFFF)) // Tint
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Location name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = operator,
                    onValueChange = { operator = it },
                    label = { Text("Company / Charity (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Accepted items", style = MaterialTheme.typography.titleSmall)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = clothing,
                        onClick = { clothing = !clothing },
                        label = { Text("Clothing") }
                    )
                    FilterChip(
                        selected = shoes,
                        onClick = { shoes = !shoes },
                        label = { Text("Shoes") }
                    )
                    FilterChip(
                        selected = electronics,
                        onClick = { electronics = !electronics },
                        label = { Text("Electronics") }
                    )
                    FilterChip(
                        selected = other,
                        onClick = { other = !other },
                        label = { Text("Other") }
                    )
                }

                Divider()

                Text(
                    text = "Location (captured from GPS)",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = "Latitude: $latText")
                Text(text = "Longitude: $lonText")

                Button(
                    onClick = {
                        pendingLocationRequest = true

                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use my current location")
                }

                Divider()

                Text(text = "Bin Photo (optional)", style = MaterialTheme.typography.titleSmall)

                photoUri?.let { uriString ->
                    Image(
                        painter = rememberAsyncImagePainter(model = uriString),
                        contentDescription = "Selected bin photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add bin photo")
                }

            }
        }
    }
}
