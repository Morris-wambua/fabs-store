package com.morrislabs.fabs_store.ui.screens.services

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.CreateServicePayload
import com.morrislabs.fabs_store.data.model.MainCategory
import com.morrislabs.fabs_store.data.model.SubCategory
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.localization.LocaleManager
import com.morrislabs.fabs_store.ui.viewmodel.ServicesViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    existingService: TypeOfServiceDTO? = null,
    onNavigateBack: () -> Unit,
    onServiceSaved: () -> Unit,
    storeViewModel: StoreViewModel = viewModel(),
    servicesViewModel: ServicesViewModel = viewModel()
) {
    val context = LocalContext.current
    val locale = LocaleManager.getActiveLocale(context)
    val storeState by storeViewModel.storeState.collectAsState()
    val saveState by servicesViewModel.saveServiceState.collectAsState()

    val storeId = when (storeState) {
        is StoreViewModel.StoreState.Success -> (storeState as StoreViewModel.StoreState.Success).data.id ?: ""
        else -> ""
    }

    val isEditMode = existingService != null

    var price by rememberSaveable { mutableStateOf("") }
    var selectedDuration by rememberSaveable { mutableStateOf(existingService?.duration ?: 60) }
    var selectedMainCategory by remember { mutableStateOf(existingService?.mainCategory ?: MainCategory.HAIR_SERVICES) }
    var selectedSubCategory by remember { mutableStateOf<SubCategory?>(existingService?.subCategory) }
    var description by rememberSaveable { mutableStateOf(existingService?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by rememberSaveable { mutableStateOf(existingService?.imageUrl ?: "") }
    var isUploading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDurationPicker by remember { mutableStateOf(false) }

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

    LaunchedEffect(existingService?.id) {
        price = existingService?.price?.toString() ?: ""
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

    if (showDurationPicker) {
        DurationPickerDialog(
            selectedDuration = selectedDuration,
            onDurationSelected = {
                selectedDuration = it
                showDurationPicker = false
            },
            onDismiss = { showDurationPicker = false }
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

                Spacer(modifier = Modifier.height(16.dp))

                PriceAndDurationRow(
                    price = price,
                    onPriceChange = { price = it },
                    selectedDuration = selectedDuration,
                    onDurationClick = { showDurationPicker = true },
                    currencyCode = existingService?.currencyCode
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            ActionButtons(
                isLoading = saveState is ServicesViewModel.SaveServiceState.Loading || isUploading,
                isEditMode = isEditMode,
                onDiscard = onNavigateBack,
                onSave = {
                    val priceValue = price.toIntOrNull() ?: 0
                    if (priceValue <= 0 || selectedSubCategory == null) {
                        errorMessage = "Please fill in all required fields (price, category)"
                        showErrorDialog = true
                        return@ActionButtons
                    }

                    val currencyCode = runCatching {
                        java.util.Currency.getInstance(locale).currencyCode
                    }.getOrDefault("USD")

                    val derivedName = selectedSubCategory!!.name.lowercase().replace("_", " ")

                    val saveAction: (String) -> Unit = { finalImageUrl ->
                        val payload = CreateServicePayload(
                            name = derivedName,
                            mainCategory = selectedMainCategory,
                            subCategory = selectedSubCategory!!,
                            price = priceValue,
                            currencyCode = currencyCode,
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
