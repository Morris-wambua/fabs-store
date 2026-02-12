package com.morrislabs.fabs_store.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StoreMallDirectory
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.Badge
import com.morrislabs.fabs_store.data.model.CreateStorePayload
import com.morrislabs.fabs_store.data.model.LocationInput
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.ui.components.ErrorDialog
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.morrislabs.fabs_store.data.model.LocationDTO

@Composable
fun CreateStoreScreen(
    onStoreCreated: () -> Unit,
    storeViewModel: StoreViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(0) }
    val context = LocalContext.current
    
    // Basic Info
    var storeName by remember { mutableStateOf("") }
    var storeUsername by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("0") }
    var storeNameError by remember { mutableStateOf<String?>(null) }
    var storeUsernameError by remember { mutableStateOf<String?>(null) }
    var discountError by remember { mutableStateOf<String?>(null) }

    // Location & Services
    var selectedLocation by remember { mutableStateOf<LocationInput?>(null) }
    var selectedServices by remember { mutableStateOf<Set<String>>(emptySet()) }

    val createStoreState by storeViewModel.createStoreState.collectAsState()
    val servicesState by storeViewModel.servicesState.collectAsState()

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Load services on step 2
    LaunchedEffect(currentStep) {
        if (currentStep == 2) {
            storeViewModel.fetchServices()
        }
    }

    LaunchedEffect(createStoreState) {
        when (createStoreState) {
            is StoreViewModel.CreateStoreState.Success -> onStoreCreated()
            is StoreViewModel.CreateStoreState.Error -> {
                errorMessage = (createStoreState as StoreViewModel.CreateStoreState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    if (showErrorDialog) {
        ErrorDialog(
            errorMessage = errorMessage,
            onDismiss = {
                showErrorDialog = false
                storeViewModel.resetCreateStoreState()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with back button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { currentStep-- },
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Setup Your Store",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f).padding(start = 16.dp)
                    )
                    Text(
                        text = "${currentStep + 1}/3",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress indicator
            LinearProgressIndicator(
                progress = { (currentStep + 1) / 3f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            // Step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = slideInHorizontally { width -> width } + fadeIn(),
                        initialContentExit = slideOutHorizontally { width -> -width } + fadeOut()
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { step ->
                when (step) {
                    0 -> StepBasicInfo(
                        storeName = storeName,
                        onStoreNameChange = { storeName = it; storeNameError = null },
                        storeNameError = storeNameError,
                        storeUsername = storeUsername,
                        onStoreUsernameChange = { storeUsername = it; storeUsernameError = null },
                        storeUsernameError = storeUsernameError,
                        discount = discount,
                        onDiscountChange = { discount = it; discountError = null },
                        discountError = discountError,
                        onNext = { storeNameError = null; storeUsernameError = null; discountError = null; currentStep++ }
                    )
                    1 -> StepInputLocation(
                        selectedLocation = selectedLocation,
                        onLocationChange = { selectedLocation = it },
                        onNext = { currentStep++ },
                        context = context
                    )
                    2 -> StepSelectServices(
                        servicesState = servicesState,
                        selectedServices = selectedServices,
                        onServiceToggle = { serviceId ->
                            selectedServices = if (selectedServices.contains(serviceId)) {
                                selectedServices - serviceId
                            } else {
                                selectedServices + serviceId
                            }
                        },
                        onCreate = {
                            if (validateAllSteps(
                                    storeName, storeUsername, discount,
                                    selectedLocation as LocationDTO?, selectedServices,
                                    { storeNameError = it }, { storeUsernameError = it },
                                    { discountError = it }
                                )) {
                                val payload = CreateStorePayload(
                                    name = storeName,
                                    username = storeUsername,
                                    badge = Badge.UNRANKED,
                                    discount = discount.toDouble(),
                                    locationId = java.util.UUID.randomUUID().toString(),
                                    servicesOfferedIds = selectedServices
                                )
                                storeViewModel.createStore(payload)
                            }
                        },
                        isCreating = createStoreState is StoreViewModel.CreateStoreState.Loading
                    )
                }
            }
        }
    }
}

@Composable
private fun StepBasicInfo(
    storeName: String,
    onStoreNameChange: (String) -> Unit,
    storeNameError: String?,
    storeUsername: String,
    onStoreUsernameChange: (String) -> Unit,
    storeUsernameError: String?,
    discount: String,
    onDiscountChange: (String) -> Unit,
    discountError: String?,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.StoreMallDirectory,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        Text(
            text = "Tell us about your salon",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        FormField(
            icon = Icons.Default.StoreMallDirectory,
            label = "Store Name",
            value = storeName,
            onValueChange = onStoreNameChange,
            error = storeNameError,
            placeholder = "e.g., John's Salon"
        )

        FormField(
            icon = Icons.Default.Tag,
            label = "Store Username",
            value = storeUsername,
            onValueChange = onStoreUsernameChange,
            error = storeUsernameError,
            placeholder = "e.g., johnsalon",
            supportingText = "Lowercase, numbers, underscores only"
        )

        FormField(
            icon = Icons.Default.Percent,
            label = "Opening Discount (%)",
            value = discount,
            onValueChange = onDiscountChange,
            error = discountError,
            placeholder = "0-100",
            keyboardType = KeyboardType.Number,
            supportingText = "Optional: Attract initial customers"
        )

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(top = 32.dp),
            enabled = storeName.isNotBlank() && storeUsername.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Next")
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StepInputLocation(
    selectedLocation: LocationInput?,
    onLocationChange: (LocationInput) -> Unit,
    onNext: () -> Unit,
    context: Context
) {
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationName by remember { mutableStateOf("") }
    var locationDescription by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var isLoadingGPS by remember { mutableStateOf(false) }
    var hasGPSError by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLoadingGPS = true
            fetchCurrentLocation(fusedLocationClient) { lat, lng ->
                latitude = lat
                longitude = lng
                isLoadingGPS = false
            }
        } else {
            hasGPSError = true
        }
    }

    LaunchedEffect(selectedLocation) {
        if (selectedLocation != null) {
            locationName = selectedLocation.name
            locationDescription = selectedLocation.description
            latitude = selectedLocation.latitude
            longitude = selectedLocation.longitude
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = "Store Location",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        Text(
            text = "Add your salon's location details",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Get Current Location Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        isLoadingGPS = true
                        fetchCurrentLocation(fusedLocationClient) { lat, lng ->
                            latitude = lat
                            longitude = lng
                            isLoadingGPS = false
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoadingGPS) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(" Detecting location...", modifier = Modifier.padding(start = 12.dp))
                } else {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(20.dp))
                    Text(" Use Current Location", modifier = Modifier.padding(start = 12.dp))
                }
            }
        }

        if (hasGPSError) {
            Text(
                "Location permission denied. Please enter manually.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Location Name
        FormField(
            icon = Icons.Default.LocationOn,
            label = "Location Name",
            value = locationName,
            onValueChange = { locationName = it },
            error = if (locationName.isBlank()) "Location name required" else null,
            placeholder = "e.g., Downtown Branch, Main Office"
        )

        // Location Description
        FormField(
            icon = Icons.Default.LocationOn,
            label = "Description",
            value = locationDescription,
            onValueChange = { locationDescription = it },
            error = if (locationDescription.isBlank()) "Description required" else null,
            placeholder = "e.g., Building 5, Ground Floor, Near Market"
        )

        // Coordinates Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Coordinates",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Latitude", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "%.6f".format(latitude),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Column {
                        Text("Longitude", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "%.6f".format(longitude),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (locationName.isNotBlank() && locationDescription.isNotBlank()) {
                    onLocationChange(
                        LocationInput(
                            name = locationName,
                            description = locationDescription,
                            latitude = latitude,
                            longitude = longitude
                        )
                    )
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(top = 24.dp),
            enabled = locationName.isNotBlank() && locationDescription.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Next")
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StepSelectServices(
    servicesState: StoreViewModel.LoadingState<List<TypeOfServiceDTO>>,
    selectedServices: Set<String>,
    onServiceToggle: (String) -> Unit,
    onCreate: () -> Unit,
    isCreating: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = "Select Services",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        Text(
            text = "Which services do you offer?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (servicesState) {
            is StoreViewModel.LoadingState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is StoreViewModel.LoadingState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(servicesState.data) { service ->
                        ServiceCheckItem(
                            service = service,
                            isChecked = selectedServices.contains(service.id),
                            onCheckedChange = { onServiceToggle(service.id) }
                        )
                    }
                }
            }
            is StoreViewModel.LoadingState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load services", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {}
        }

        Button(
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(top = 24.dp),
            enabled = selectedServices.isNotEmpty() && !isCreating,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).padding(end = 8.dp)
                    )
                    Text("Create Store")
                }
            }
        }
    }
}

private fun fetchCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit
) {
    try {
        @Suppress("MissingPermission")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                }
            }
    } catch (e: Exception) {
        // Handle error silently, user can enter manually
    }
}

@Composable
private fun ServiceCheckItem(
    service: TypeOfServiceDTO,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isChecked) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "₹${service.price}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${service.mainCategory}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun FormField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp).padding(end = 12.dp),
                tint = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 36.dp, top = 6.dp)
            )
        } else if (supportingText != null) {
            Text(
                text = supportingText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 36.dp, top = 6.dp)
            )
        }
    }
}

private fun validateAllSteps(
    storeName: String,
    storeUsername: String,
    discount: String,
    selectedLocation: LocationDTO?,
    selectedServices: Set<String>,
    setNameError: (String?) -> Unit,
    setUsernameError: (String?) -> Unit,
    setDiscountError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (storeName.isBlank() || storeName.length < 3) {
        setNameError("Store name required (min 3 chars)")
        isValid = false
    } else {
        setNameError(null)
    }

    if (storeUsername.isBlank() || !storeUsername.matches(Regex("[a-z0-9_]*"))) {
        setUsernameError("Username required (lowercase, numbers, underscores)")
        isValid = false
    } else {
        setUsernameError(null)
    }

    val discountValue = discount.toDoubleOrNull()
    if (discountValue == null || discountValue < 0 || discountValue > 100) {
        setDiscountError("Invalid discount")
        isValid = false
    } else {
        setDiscountError(null)
    }

    return isValid && selectedLocation != null && selectedServices.isNotEmpty()
}
