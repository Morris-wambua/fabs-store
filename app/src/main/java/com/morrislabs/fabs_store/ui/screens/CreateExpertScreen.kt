package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.CreateExpertPayload
import com.morrislabs.fabs_store.data.model.SubCategory
import com.morrislabs.fabs_store.ui.viewmodel.ExpertViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExpertScreen(
    onNavigateBack: () -> Unit,
    onExpertCreated: () -> Unit,
    storeViewModel: StoreViewModel = viewModel(),
    expertViewModel: ExpertViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val createExpertState by expertViewModel.createExpertState.collectAsState()

    val storeId = when (storeState) {
        is StoreViewModel.StoreState.Success -> (storeState as StoreViewModel.StoreState.Success).data.id ?: ""
        else -> ""
    }

    val availableSpecializations = when (storeState) {
        is StoreViewModel.StoreState.Success -> {
            val store = (storeState as StoreViewModel.StoreState.Success).data
            store.servicesOffered.map { it.subCategory }.distinct()
        }
        else -> emptyList()
    }

    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var yearsOfExperience by rememberSaveable { mutableStateOf("") }
    var selectedSpecializations by remember { mutableStateOf<List<SubCategory>>(emptyList()) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(createExpertState) {
        when (createExpertState) {
            is ExpertViewModel.CreateExpertState.Success -> {
                expertViewModel.resetCreateExpertState()
                onExpertCreated()
            }
            is ExpertViewModel.CreateExpertState.Error -> {
                errorMessage = (createExpertState as ExpertViewModel.CreateExpertState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            expertViewModel.resetCreateExpertState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expert") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (storeId.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Store not loaded. Please go back and try again.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            CreateExpertForm(
                name = name,
                onNameChange = { name = it },
                phone = phone,
                onPhoneChange = { phone = it },
                bio = bio,
                onBioChange = { bio = it },
                yearsOfExperience = yearsOfExperience,
                onYearsOfExperienceChange = { yearsOfExperience = it },
                selectedSpecializations = selectedSpecializations,
                availableSpecializations = availableSpecializations,
                onSpecializationToggle = { specialization ->
                    selectedSpecializations = if (selectedSpecializations.contains(specialization)) {
                        selectedSpecializations - specialization
                    } else {
                        selectedSpecializations + specialization
                    }
                },
                isLoading = createExpertState is ExpertViewModel.CreateExpertState.Loading,
                onSubmit = {
                    val payload = CreateExpertPayload(
                        name = name.trim(),
                        bio = bio.trim().ifEmpty { null },
                        specialization = selectedSpecializations,
                        contacts = phone.trim(),
                        storeId = storeId,
                        yearsOfExperience = yearsOfExperience.toIntOrNull(),
                        isAvailable = true
                    )
                    expertViewModel.createExpert(storeId, payload)
                },
                onCancel = onNavigateBack,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
