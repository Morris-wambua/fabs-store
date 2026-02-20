package com.morrislabs.fabs_store.ui.screens.services

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.morrislabs.fabs_store.data.model.CreateServicePayload
import com.morrislabs.fabs_store.data.model.MainCategory
import com.morrislabs.fabs_store.data.model.SubCategory
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.ui.viewmodel.ServicesViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

private val GreenAccent = Color(0xFF22C55E)
private val LightGreenBg = Color(0xFFF0FDF4)
private val DurationOptions = listOf(30, 45, 60, 90, 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    existingService: TypeOfServiceDTO? = null,
    onNavigateBack: () -> Unit,
    onServiceSaved: () -> Unit,
    storeViewModel: StoreViewModel = viewModel(),
    servicesViewModel: ServicesViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val saveState by servicesViewModel.saveServiceState.collectAsState()

    val storeId = when (storeState) {
        is StoreViewModel.StoreState.Success -> (storeState as StoreViewModel.StoreState.Success).data.id ?: ""
        else -> ""
    }

    val isEditMode = existingService != null

    var name by rememberSaveable { mutableStateOf(existingService?.name ?: "") }
    var price by rememberSaveable { mutableStateOf(existingService?.price?.toString() ?: "") }
    var selectedDuration by rememberSaveable { mutableStateOf(existingService?.duration ?: 60) }
    var selectedMainCategory by remember { mutableStateOf(existingService?.mainCategory ?: MainCategory.HAIR_SERVICES) }
    var selectedSubCategory by remember { mutableStateOf<SubCategory?>(existingService?.subCategory) }
    var description by rememberSaveable { mutableStateOf(existingService?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by rememberSaveable { mutableStateOf(existingService?.imageUrl ?: "") }
    var isUploading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val subCategories = remember(selectedMainCategory) {
        servicesViewModel.getSubCategoriesForMain(selectedMainCategory)
    }

    LaunchedEffect(selectedMainCategory) {
        if (selectedSubCategory == null || selectedSubCategory?.toMainCategory() != selectedMainCategory) {
            selectedSubCategory = subCategories.firstOrNull()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imageUri = it } }

    LaunchedEffect(saveState) {
        when (saveState) {
            is ServicesViewModel.SaveServiceState.Success -> {
                servicesViewModel.resetSaveState()
                onServiceSaved()
            }
            is ServicesViewModel.SaveServiceState.Error -> {
                errorMessage = (saveState as ServicesViewModel.SaveServiceState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose { servicesViewModel.resetSaveState() }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Service" else "Add New Service",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                ImageUploadSection(
                    imageUri = imageUri,
                    imageUrl = imageUrl.ifEmpty { null },
                    isUploading = isUploading,
                    onSelectImage = { galleryLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                ServiceNameField(name = name, onNameChange = { name = it })

                Spacer(modifier = Modifier.height(16.dp))

                PriceAndDurationRow(
                    price = price,
                    onPriceChange = { price = it },
                    selectedDuration = selectedDuration,
                    onDurationChange = { selectedDuration = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CategorySection(
                    selectedMainCategory = selectedMainCategory,
                    onMainCategoryChange = { selectedMainCategory = it },
                    selectedSubCategory = selectedSubCategory,
                    onSubCategoryChange = { selectedSubCategory = it },
                    subCategories = subCategories
                )

                Spacer(modifier = Modifier.height(16.dp))

                DescriptionField(
                    description = description,
                    onDescriptionChange = { description = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            ActionButtons(
                isLoading = saveState is ServicesViewModel.SaveServiceState.Loading || isUploading,
                isEditMode = isEditMode,
                onDiscard = onNavigateBack,
                onSave = {
                    val priceValue = price.toIntOrNull() ?: 0
                    if (name.isBlank() || priceValue <= 0 || selectedSubCategory == null) {
                        errorMessage = "Please fill in all required fields (name, price, category)"
                        showErrorDialog = true
                        return@ActionButtons
                    }

                    val saveAction: (String) -> Unit = { finalImageUrl ->
                        val payload = CreateServicePayload(
                            name = name,
                            mainCategory = selectedMainCategory,
                            subCategory = selectedSubCategory!!,
                            price = priceValue,
                            duration = selectedDuration,
                            description = description.ifBlank { null },
                            imageUrl = finalImageUrl.ifBlank { null }
                        )

                        if (isEditMode) {
                            servicesViewModel.updateService(storeId, existingService!!.id, payload)
                        } else {
                            servicesViewModel.createService(storeId, payload)
                        }
                    }

                    if (imageUri != null) {
                        isUploading = true
                        servicesViewModel.uploadServiceImage(
                            uri = imageUri!!,
                            onSuccess = { url, _ ->
                                isUploading = false
                                imageUrl = url
                                saveAction(url)
                            },
                            onFailure = { msg ->
                                isUploading = false
                                errorMessage = msg
                                showErrorDialog = true
                            }
                        )
                    } else {
                        saveAction(imageUrl)
                    }
                }
            )
        }
    }
}

@Composable
private fun ImageUploadSection(
    imageUri: Uri?,
    imageUrl: String?,
    isUploading: Boolean,
    onSelectImage: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LightGreenBg)
            .border(
                width = 2.dp,
                color = GreenAccent.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelectImage),
        contentAlignment = Alignment.Center
    ) {
        when {
            isUploading -> CircularProgressIndicator(color = GreenAccent)
            imageUri != null -> {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Service image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            imageUrl != null -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Service image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LightGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Upload",
                            tint = GreenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Upload Service Image",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Recommended size: 800x600px",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenAccent)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Select File", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceNameField(name: String, onNameChange: (String) -> Unit) {
    Text(
        "Service Name",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = { Text("e.g., Premium Haircut") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
private fun PriceAndDurationRow(
    price: String,
    onPriceChange: (String) -> Unit,
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Price (KES)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { if (it.all { c -> c.isDigit() }) onPriceChange(it) },
                placeholder = { Text("0") },
                prefix = { Text("KES ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Duration (min)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            DurationSelector(
                selectedDuration = selectedDuration,
                onDurationChange = onDurationChange
            )
        }
    }
}

@Composable
private fun DurationSelector(
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DurationOptions.forEach { duration ->
            val isSelected = duration == selectedDuration
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isSelected) Modifier.background(MaterialTheme.colorScheme.surface)
                        else Modifier
                    )
                    .clickable { onDurationChange(duration) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$duration",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) GreenAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySection(
    selectedMainCategory: MainCategory,
    onMainCategoryChange: (MainCategory) -> Unit,
    selectedSubCategory: SubCategory?,
    onSubCategoryChange: (SubCategory) -> Unit,
    subCategories: List<SubCategory>
) {
    Text(
        "Category",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MainCategory.entries.forEach { category ->
            val isSelected = category == selectedMainCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(LightGreenBg)
                                .border(1.dp, GreenAccent, RoundedCornerShape(20.dp))
                        } else {
                            Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        }
                    )
                    .clickable { onMainCategoryChange(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = formatCategoryName(category.name),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) GreenAccent else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        "Sub Category",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedSubCategory?.let { formatCategoryName(it.name) } ?: "Select sub category",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            subCategories.forEach { sub ->
                DropdownMenuItem(
                    text = { Text(formatCategoryName(sub.name)) },
                    onClick = {
                        onSubCategoryChange(sub)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Text(
        "Description",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        placeholder = { Text("Describe what this service includes, special techniques, or products used...") },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        maxLines = 5
    )
}

@Composable
private fun ActionButtons(
    isLoading: Boolean,
    isEditMode: Boolean,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    HorizontalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDiscard,
            modifier = Modifier.weight(0.35f),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Discard")
        }

        Button(
            onClick = onSave,
            modifier = Modifier.weight(0.65f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
            contentPadding = PaddingValues(vertical = 14.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEditMode) "Save Changes" else "Save Service",
                    color = Color.White
                )
            }
        }
    }
}

private fun formatCategoryName(name: String): String {
    return name.split("_").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}
