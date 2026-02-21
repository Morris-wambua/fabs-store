package com.morrislabs.fabs_store.ui.screens.services

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.CreateServicePayload
import com.morrislabs.fabs_store.data.model.SubCategory
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.data.model.toDisplayName
import com.morrislabs.fabs_store.ui.viewmodel.ServicesViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(
    service: TypeOfServiceDTO,
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

    var price by rememberSaveable { mutableStateOf(service.price.toString()) }
    var description by rememberSaveable { mutableStateOf(service.description ?: "") }
    var selectedMainCategory by remember { mutableStateOf(service.mainCategory) }
    var selectedSubCategory by remember { mutableStateOf(service.subCategory) }
    var selectedDuration by rememberSaveable { mutableStateOf(service.duration ?: 60) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by rememberSaveable { mutableStateOf(service.imageUrl ?: "") }
    var isUploading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val subCategories = remember(selectedMainCategory) {
        servicesViewModel.getSubCategoriesForMain(selectedMainCategory)
    }

    LaunchedEffect(selectedMainCategory) {
        if (selectedSubCategory.toMainCategory() != selectedMainCategory) {
            selectedSubCategory = subCategories.firstOrNull() ?: selectedSubCategory
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
                        "Service Details",
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
                .verticalScroll(rememberScrollState())
        ) {
            ImageSection(
                imageUri = imageUri,
                imageUrl = imageUrl.ifEmpty { null },
                onEditClick = { galleryLauncher.launch("image/*") }
            )

            TitleCard(
                subCategoryName = selectedSubCategory.toDisplayName(),
                description = description
            )

            Spacer(modifier = Modifier.height(12.dp))

            GeneralInfoCard(
                price = price,
                onPriceChange = { if (it.all { c -> c.isDigit() }) price = it },
                description = description,
                onDescriptionChange = { description = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategoryCard(
                selectedMainCategory = selectedMainCategory,
                onMainCategoryChange = { selectedMainCategory = it },
                selectedSubCategory = selectedSubCategory,
                onSubCategoryChange = { selectedSubCategory = it },
                subCategories = subCategories
            )

            Spacer(modifier = Modifier.height(12.dp))

            DurationCard(
                selectedDuration = selectedDuration,
                onDurationChange = { selectedDuration = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SaveButton(
                isLoading = saveState is ServicesViewModel.SaveServiceState.Loading || isUploading,
                onSave = {
                    val priceValue = price.toIntOrNull() ?: 0
                    if (priceValue <= 0) {
                        errorMessage = "Please enter a valid price"
                        showErrorDialog = true
                        return@SaveButton
                    }

                    val derivedName = selectedSubCategory.name.lowercase().replace("_", " ")

                    val saveAction: (String) -> Unit = { finalImageUrl ->
                        val payload = CreateServicePayload(
                            name = derivedName,
                            mainCategory = selectedMainCategory,
                            subCategory = selectedSubCategory,
                            price = priceValue,
                            duration = selectedDuration,
                            description = description.ifBlank { null },
                            imageUrl = finalImageUrl.ifBlank { null }
                        )
                        servicesViewModel.updateService(storeId, service.id, payload)
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
