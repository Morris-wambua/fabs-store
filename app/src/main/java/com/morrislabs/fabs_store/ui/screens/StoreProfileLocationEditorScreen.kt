package com.morrislabs.fabs_store.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.morrislabs.fabs_store.data.model.LocationDTO
import com.morrislabs.fabs_store.data.model.UpdateStorePayload
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

private val defaultLocation = LatLng(-1.2921, 36.8219)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StoreProfileLocationEditorScreen(
    onNavigateBack: () -> Unit,
    storeViewModel: StoreViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val state by storeViewModel.storeState.collectAsState()
    val updateState by storeViewModel.updateStoreState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var initialized by remember { mutableStateOf(false) }
    var saveRequested by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var detectedAddress by remember { mutableStateOf("Move map to select location") }
    var storeId by remember { mutableStateOf<String?>(null) }
    var locationId by remember { mutableStateOf<String?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        storeViewModel.fetchUserStore()
    }

    LaunchedEffect(state) {
        if (state is StoreViewModel.StoreState.Success && !initialized) {
            val store = (state as StoreViewModel.StoreState.Success).data
            storeId = store.id
            locationId = store.locationDTO?.id
            val current = store.locationDTO
            if (current != null) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(current.latitude, current.longitude),
                    16f
                )
                detectedAddress = current.description ?: current.name
            }
            initialized = true
        }
    }

    LaunchedEffect(updateState, saveRequested) {
        if (!saveRequested) {
            return@LaunchedEffect
        }
        when (updateState) {
            is StoreViewModel.UpdateStoreState.Success -> {
                saveRequested = false
                storeViewModel.resetUpdateStoreState()
                onNavigateBack()
            }
            is StoreViewModel.UpdateStoreState.Error -> {
                saveRequested = false
                storeViewModel.resetUpdateStoreState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.isMoving }.collect { isMoving ->
            if (isMoving) {
                return@collect
            }
            val target = cameraPositionState.position.target
            withContext(Dispatchers.IO) {
                detectedAddress = try {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(target.latitude, target.longitude, 1)
                        ?.firstOrNull()
                        ?.getAddressLine(0)
                        ?: "Lat ${"%.4f".format(target.latitude)}, Lng ${"%.4f".format(target.longitude)}"
                } catch (_: Exception) {
                    "Lat ${"%.4f".format(target.latitude)}, Lng ${"%.4f".format(target.longitude)}"
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                    Text(
                        text = "Edit Location",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search for address...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                try {
                                    @Suppress("DEPRECATION")
                                    Geocoder(context, Locale.getDefault()).getFromLocationName(searchQuery, 1)
                                } catch (_: Exception) {
                                    null
                                }
                            }?.firstOrNull()
                            if (result != null) {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(result.latitude, result.longitude),
                                        16f
                                    )
                                )
                            }
                        }
                    }),
                    singleLine = true
                )
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(12.dp)
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
                .offset(y = 96.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface
        ) {
            IconButton(onClick = {
                if (permissionState.status.isGranted) {
                    @SuppressLint("MissingPermission")
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(location.latitude, location.longitude),
                                        16f
                                    )
                                )
                            }
                        }
                    }
                } else if (!permissionState.status.shouldShowRationale) {
                    permissionState.launchPermissionRequest()
                } else {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                }
            }) {
                Icon(Icons.Default.GpsFixed, contentDescription = "My location")
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SELECTED ADDRESS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(detectedAddress, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val id = storeId ?: return@Button
                        val target = cameraPositionState.position.target
                        val name = detectedAddress.split(",").firstOrNull()?.trim().orEmpty().ifBlank { "Selected Location" }
                        saveRequested = true
                        storeViewModel.updateStore(
                            id,
                            UpdateStorePayload(
                                location = LocationDTO(
                                    id = locationId ?: UUID.randomUUID().toString(),
                                    name = name,
                                    description = detectedAddress,
                                    latitude = target.latitude,
                                    longitude = target.longitude
                                )
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirm Location", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
