package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.morrislabs.fabs_store.data.model.FetchStoreResponse
import com.morrislabs.fabs_store.data.model.LocationDTO
import com.morrislabs.fabs_store.data.model.UpdateStorePayload
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@Composable
fun StoreProfileEditorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditLocation: () -> Unit,
    storeViewModel: StoreViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val updateStoreState by storeViewModel.updateStoreState.collectAsState()
    
    var storeData by remember { mutableStateOf<FetchStoreResponse?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Form state
    var storeName by remember { mutableStateOf("") }
    var storeUsername by remember { mutableStateOf("") }
    var storeDescription by remember { mutableStateOf("") }
    var openingHours by remember { mutableStateOf("") }
    var closingHours by remember { mutableStateOf("") }

    // Fetch store data on initial composition
    LaunchedEffect(Unit) {
        storeViewModel.fetchUserStore()
    }

    // Initialize store data when storeState changes
    LaunchedEffect(storeState) {
        when (storeState) {
            is StoreViewModel.StoreState.Success -> {
                val store = (storeState as StoreViewModel.StoreState.Success).data
                storeData = store
                if (storeName.isEmpty()) {
                    storeName = store.name
                    storeUsername = store.username
                    storeDescription = "" // Will be loaded from backend description field if available
                }
            }
            else -> {}
        }
    }

    // Handle update state changes
    LaunchedEffect(updateStoreState) {
        when (updateStoreState) {
            is StoreViewModel.UpdateStoreState.Success -> {
                isSaving = false
                isEditing = false
            }
            is StoreViewModel.UpdateStoreState.Loading -> {
                isSaving = true
            }
            is StoreViewModel.UpdateStoreState.Error -> {
                isSaving = false
                // TODO: Show error message
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        StoreProfileHeader(
            title = "Store Profile",
            onNavigateBack = onNavigateBack,
            isEditing = isEditing,
            onEditToggle = { isEditing = !isEditing },
            isSaving = isSaving
        )

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (storeData != null) {
                // Store Basic Info Section
                StoreBasicInfoSection(
                    storeName = storeName.ifEmpty { storeData!!.name },
                    storeUsername = storeUsername.ifEmpty { storeData!!.username },
                    storeDescription = storeDescription,
                    isEditing = isEditing,
                    onNameChange = { storeName = it },
                    onUsernameChange = { storeUsername = it },
                    onDescriptionChange = { storeDescription = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Store Posts Section
                StorePostsSection(
                    storeId = storeData?.id ?: "",
                    isEditing = isEditing
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Location Section
                StoreLocationSection(
                    location = storeData!!.locationDTO,
                    isEditing = isEditing,
                    onEditLocation = onNavigateToEditLocation
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Opening Hours Section
                StoreOpeningHoursSection(
                    openingHours = openingHours,
                    closingHours = closingHours,
                    isEditing = isEditing,
                    onOpeningHoursChange = { openingHours = it },
                    onClosingHoursChange = { closingHours = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Action Buttons
        if (isEditing) {
            StoreProfileActionButtons(
                isSaving = isSaving,
                onSave = {
                    if (storeData != null) {
                        val locationPayload = storeData!!.locationDTO?.copy(
                            description = storeData!!.locationDTO!!.description ?: ""
                        )
                        val updatePayload = UpdateStorePayload(
                            name = storeName.ifEmpty { storeData!!.name },
                            description = storeDescription.ifEmpty { "" },
                            username = storeUsername.ifEmpty { storeData!!.username },
                            noOfExperts = storeData!!.noOfExperts,
                            ratings = storeData!!.ratings,
                            badge = storeData!!.badge,
                            discount = storeData!!.discount,
                            location = locationPayload,
                            servicesOffered = emptyList()
                        )
                        storeViewModel.updateStore(storeData!!.id ?: "", updatePayload)
                    }
                },
                onCancel = {
                    isEditing = false
                    // Reset form
                    if (storeData != null) {
                        storeName = storeData!!.name
                        storeUsername = storeData!!.username
                    }
                    storeDescription = ""
                    openingHours = ""
                    closingHours = ""
                }
            )
        }
    }
}

// ===========================
// Section Components
// ===========================

@Composable
private fun StoreBasicInfoSection(
    storeName: String,
    storeUsername: String,
    storeDescription: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    Column {
        SectionTitle("Basic Information")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = onNameChange,
                        label = { Text("Store Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = storeUsername,
                        onValueChange = onUsernameChange,
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = storeDescription,
                        onValueChange = onDescriptionChange,
                        label = { Text("About Your Store") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4,
                        placeholder = { Text("Tell customers about your store, services, and specialties") }
                    )
                } else {
                    InfoRow(label = "Store Name", value = storeName)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(label = "Username", value = storeUsername)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(label = "About", value = storeDescription.ifEmpty { "No description" })
                }
            }
        }
    }
}

@Composable
private fun StorePostsSection(
    storeId: String,
    isEditing: Boolean
) {
    var showCreatePostDialog by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Store Posts",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            if (isEditing) {
                IconButton(onClick = { showCreatePostDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Post",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create and manage store posts to showcase your business",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
    
    // TODO: Implement CreateStorePostDialog when ready
    // if (showCreatePostDialog) {
    //     CreateStorePostDialog(
    //         storeId = storeId,
    //         onDismiss = { showCreatePostDialog = false },
    //         onPostCreated = { showCreatePostDialog = false }
    //     )
    // }
}

@Composable
private fun StoreLocationSection(
    location: com.morrislabs.fabs_store.data.model.LocationDTO?,
    isEditing: Boolean,
    onEditLocation: () -> Unit
) {
    Column {
        SectionTitle("Location")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (location != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = location.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (location.description != null) {
                                Text(
                                    text = location.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isEditing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onEditLocation,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Edit Location")
                        }
                    }
                } else {
                    Text("No location set")
                }
            }
        }
    }
}

@Composable
private fun StoreOpeningHoursSection(
    openingHours: String,
    closingHours: String,
    isEditing: Boolean,
    onOpeningHoursChange: (String) -> Unit,
    onClosingHoursChange: (String) -> Unit
) {
    Column {
        SectionTitle("Opening Hours")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = openingHours,
                        onValueChange = onOpeningHoursChange,
                        label = { Text("Opening Time (e.g., 08:00 AM)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = closingHours,
                        onValueChange = onClosingHoursChange,
                        label = { Text("Closing Time (e.g., 06:00 PM)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    InfoRow(
                        label = "Opens",
                        value = openingHours.ifEmpty { "Not set" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(
                        label = "Closes",
                        value = closingHours.ifEmpty { "Not set" }
                    )
                }
            }
        }
    }
}

// ===========================
// Header and Action Components
// ===========================

@Composable
private fun StoreProfileHeader(
    title: String,
    onNavigateBack: () -> Unit,
    isEditing: Boolean,
    onEditToggle: () -> Unit,
    isSaving: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )

            if (!isEditing && !isSaving) {
                IconButton(onClick = onEditToggle) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreProfileActionButtons(
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}

// ===========================
// Reusable UI Components
// ===========================

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
