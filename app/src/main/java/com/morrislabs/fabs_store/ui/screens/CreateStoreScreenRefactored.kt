package com.morrislabs.fabs_store.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.StoreMallDirectory
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.Badge
import com.morrislabs.fabs_store.data.model.CreateStorePayload
import com.morrislabs.fabs_store.data.model.LocationDTO
import com.morrislabs.fabs_store.data.model.LocationInput
import com.morrislabs.fabs_store.data.model.MainCategory
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.ui.components.ErrorDialog
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@Composable
fun CreateStoreScreenRefactored(
    onStoreCreated: () -> Unit,
    storeViewModel: StoreViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(0) }
    val context = LocalContext.current
    
    var storeName by remember { mutableStateOf("") }
    var storeUsername by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("0") }
    var storeNameError by remember { mutableStateOf<String?>(null) }
    var storeUsernameError by remember { mutableStateOf<String?>(null) }
    var discountError by remember { mutableStateOf<String?>(null) }

    var locationName by remember { mutableStateOf("") }
    var locationDescription by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf<String?>(null) }

    var selectedCategory by remember { mutableStateOf<MainCategory?>(null) }
    var selectedServices by remember { mutableStateOf<Set<String>>(emptySet()) }

    val createStoreState by storeViewModel.createStoreState.collectAsState()
    val categoriesState by storeViewModel.categoriesState.collectAsState()
    val servicesByCategoryState by storeViewModel.servicesByCategoryState.collectAsState()

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(currentStep) {
        when (currentStep) {
            1 -> storeViewModel.fetchCategories()
            2 -> {
                if (selectedCategory != null) {
                    storeViewModel.fetchServicesByCategory(selectedCategory!!)
                }
            }
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

            LinearProgressIndicator(
                progress = { (currentStep + 1) / 3f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

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
                    0 -> StepBasicAndLocation(
                        storeName = storeName,
                        onStoreNameChange = { storeName = it; storeNameError = null },
                        storeNameError = storeNameError,
                        storeUsername = storeUsername,
                        onStoreUsernameChange = { storeUsername = it; storeUsernameError = null },
                        storeUsernameError = storeUsernameError,
                        discount = discount,
                        onDiscountChange = { discount = it; discountError = null },
                        discountError = discountError,
                        locationName = locationName,
                        onLocationNameChange = { locationName = it; locationError = null },
                        locationDescription = locationDescription,
                        onLocationDescriptionChange = { locationDescription = it; locationError = null },
                        latitude = latitude,
                        onLatitudeChange = { latitude = it; locationError = null },
                        longitude = longitude,
                        onLongitudeChange = { longitude = it; locationError = null },
                        locationError = locationError,
                        context = context,
                        onNext = {
                            if (validateStep0(
                                storeName, storeUsername, discount,
                                locationName, latitude, longitude,
                                { storeNameError = it }, { storeUsernameError = it },
                                { discountError = it }, { locationError = it }
                            )) {
                                currentStep++
                            }
                        }
                    )
                    1 -> StepSelectCategory(
                        categoriesState = categoriesState,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        onNext = { if (selectedCategory != null) currentStep++ }
                    )
                    2 -> StepSelectServicesByCategory(
                        servicesByCategoryState = servicesByCategoryState,
                        selectedServices = selectedServices,
                        onServiceToggle = { serviceId ->
                            selectedServices = if (selectedServices.contains(serviceId)) {
                                selectedServices - serviceId
                            } else {
                                selectedServices + serviceId
                            }
                        },
                        onCreate = {
                            val location = LocationDTO(
                                id = "",
                                name = locationName,
                                description = locationDescription,
                                latitude = latitude.toDouble(),
                                longitude = longitude.toDouble()
                            )
                            val payload = CreateStorePayload(
                                name = storeName,
                                username = storeUsername,
                                badge = Badge.SILVER,
                                discount = discount.toDouble(),
                                location = location,
                                servicesOffered = selectedServices.toList()
                            )
                            storeViewModel.createStore(payload)
                        },
                        isCreating = createStoreState is StoreViewModel.CreateStoreState.Loading
                    )
                }
            }
        }
    }
}

@Composable
private fun StepBasicAndLocation(
    storeName: String,
    onStoreNameChange: (String) -> Unit,
    storeNameError: String?,
    storeUsername: String,
    onStoreUsernameChange: (String) -> Unit,
    storeUsernameError: String?,
    discount: String,
    onDiscountChange: (String) -> Unit,
    discountError: String?,
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    locationDescription: String,
    onLocationDescriptionChange: (String) -> Unit,
    latitude: String,
    onLatitudeChange: (String) -> Unit,
    longitude: String,
    onLongitudeChange: (String) -> Unit,
    locationError: String?,
    context: Context,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(12.dp)
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
            text = "Store Information",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 16.dp)
        )

        FormField(
            icon = Icons.Default.StoreMallDirectory,
            label = "Store Name",
            value = storeName,
            onValueChange = onStoreNameChange,
            error = storeNameError,
            placeholder = "Enter your store name"
        )

        FormField(
            icon = Icons.Default.StoreMallDirectory,
            label = "Username",
            value = storeUsername,
            onValueChange = onStoreUsernameChange,
            error = storeUsernameError,
            placeholder = "lowercase_username",
            supportingText = "Only lowercase letters, numbers, and underscores"
        )

        FormField(
            icon = Icons.Default.StoreMallDirectory,
            label = "Discount (%)",
            value = discount,
            onValueChange = onDiscountChange,
            error = discountError,
            placeholder = "0-100",
            keyboardType = KeyboardType.Number
        )

        Text(
            text = "Location Details",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 8.dp)
        )

        FormField(
            icon = Icons.Default.LocationOn,
            label = "Location Name",
            value = locationName,
            onValueChange = onLocationNameChange,
            error = locationError,
            placeholder = "e.g., Downtown Branch"
        )

        FormField(
            icon = Icons.Default.LocationOn,
            label = "Description",
            value = locationDescription,
            onValueChange = onLocationDescriptionChange,
            error = null,
            placeholder = "e.g., Near City Center"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(
                icon = Icons.Default.LocationOn,
                label = "Latitude",
                value = latitude,
                onValueChange = onLatitudeChange,
                error = null,
                placeholder = "0.0",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
            FormField(
                icon = Icons.Default.LocationOn,
                label = "Longitude",
                value = longitude,
                onValueChange = onLongitudeChange,
                error = null,
                placeholder = "0.0",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fetchCurrentLocation(fusedLocationClient) { lat, lon ->
                    onLatitudeChange(lat.toString())
                    onLongitudeChange(lon.toString())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Current Location")
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun StepSelectCategory(
    categoriesState: StoreViewModel.LoadingState<List<MainCategory>>,
    selectedCategory: MainCategory?,
    onCategorySelect: (MainCategory) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select Service Category",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "What services does your store offer?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when (categoriesState) {
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
                    items(categoriesState.data) { category ->
                        CategoryCard(
                            category = category,
                            isSelected = selectedCategory == category,
                            onSelect = { onCategorySelect(category) }
                        )
                    }
                }
            }
            is StoreViewModel.LoadingState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load categories", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {}
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = selectedCategory != null,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun StepSelectServicesByCategory(
    servicesByCategoryState: StoreViewModel.LoadingState<List<TypeOfServiceDTO>>,
    selectedServices: Set<String>,
    onServiceToggle: (String) -> Unit,
    onCreate: () -> Unit,
    isCreating: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select Services",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Which services do you offer?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when (servicesByCategoryState) {
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
                    items(servicesByCategoryState.data) { service ->
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
                .height(50.dp),
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

@Composable
private fun CategoryCard(
    category: MainCategory,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name.replace("_", " "),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
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
    supportingText: String? = null,
    modifier: Modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
) {
    Column(modifier = modifier) {
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

private fun validateStep0(
    storeName: String,
    storeUsername: String,
    discount: String,
    locationName: String,
    latitude: String,
    longitude: String,
    setNameError: (String?) -> Unit,
    setUsernameError: (String?) -> Unit,
    setDiscountError: (String?) -> Unit,
    setLocationError: (String?) -> Unit
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
        setDiscountError("Invalid discount (0-100)")
        isValid = false
    } else {
        setDiscountError(null)
    }

    if (locationName.isBlank() || latitude.isBlank() || longitude.isBlank()) {
        setLocationError("Location name, latitude, and longitude required")
        isValid = false
    } else {
        val lat = latitude.toDoubleOrNull()
        val lon = longitude.toDoubleOrNull()
        if (lat == null || lon == null || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            setLocationError("Invalid coordinates")
            isValid = false
        } else {
            setLocationError(null)
        }
    }

    return isValid
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
