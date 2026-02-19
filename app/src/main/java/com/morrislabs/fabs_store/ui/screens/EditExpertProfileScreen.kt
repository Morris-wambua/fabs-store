package com.morrislabs.fabs_store.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.morrislabs.fabs_store.data.model.CreateExpertPayload
import com.morrislabs.fabs_store.ui.screens.expert.AvailabilityLeaveSection
import com.morrislabs.fabs_store.ui.screens.expert.EditExpertPhotoSheet
import com.morrislabs.fabs_store.ui.viewmodel.ExpertViewModel
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpertProfileScreen(
    expertId: String,
    storeId: String,
    onNavigateBack: () -> Unit,
    expertViewModel: ExpertViewModel = viewModel()
) {
    val expertDetailsState by expertViewModel.expertDetailsState.collectAsState()
    val updateExpertState by expertViewModel.updateExpertState.collectAsState()
    val deleteExpertState by expertViewModel.deleteExpertState.collectAsState()
    val expertLeavesState by expertViewModel.expertLeavesState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var yearsOfExperience by rememberSaveable { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var rangeStart by remember { mutableStateOf<LocalDate?>(null) }
    var rangeEnd by remember { mutableStateOf<LocalDate?>(null) }
    var isInitialized by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { photoUri = it } }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraImageUri?.let { photoUri = it } }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "expert_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(expertId) {
        expertViewModel.getExpertDetails(expertId)
        expertViewModel.getExpertLeaves(expertId)
    }

    LaunchedEffect(expertDetailsState) {
        if (!isInitialized && expertDetailsState is ExpertViewModel.ExpertDetailsState.Success) {
            val expert = (expertDetailsState as ExpertViewModel.ExpertDetailsState.Success).expert
            name = expert.name
            bio = expert.bio ?: ""
            yearsOfExperience = expert.yearsOfExperience?.toString() ?: ""
            isAvailable = expert.isAvailable
            imageUrl = expert.imageUrl
            isInitialized = true
        }
    }

    LaunchedEffect(updateExpertState) {
        if (updateExpertState is ExpertViewModel.UpdateExpertState.Success) {
            expertViewModel.resetUpdateExpertState()
            onNavigateBack()
        }
    }

    LaunchedEffect(deleteExpertState) {
        if (deleteExpertState is ExpertViewModel.DeleteExpertState.Success) {
            expertViewModel.resetDeleteExpertState()
            onNavigateBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            expertViewModel.resetUpdateExpertState()
            expertViewModel.resetDeleteExpertState()
        }
    }

    val leaveDates = remember(expertLeavesState) {
        when (expertLeavesState) {
            is ExpertViewModel.ExpertLeavesState.Success -> {
                (expertLeavesState as ExpertViewModel.ExpertLeavesState.Success).leaves
                    .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                    .toSet()
            }
            else -> emptySet()
        }
    }

    val onDateClick: (LocalDate) -> Unit = { date ->
        when {
            rangeStart == null -> rangeStart = date
            rangeEnd == null -> {
                if (date.isBefore(rangeStart)) {
                    rangeEnd = rangeStart
                    rangeStart = date
                } else {
                    rangeEnd = date
                }
            }
            else -> {
                rangeStart = date
                rangeEnd = null
            }
        }
    }

    when (expertDetailsState) {
        is ExpertViewModel.ExpertDetailsState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ExpertViewModel.ExpertDetailsState.Error -> {
            val errorMessage = (expertDetailsState as ExpertViewModel.ExpertDetailsState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "Error: $errorMessage", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { expertViewModel.getExpertDetails(expertId) }) { Text("Retry") }
                }
            }
        }
        is ExpertViewModel.ExpertDetailsState.Success -> {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.CenterStart)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = "Edit Expert Profile",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    ProfilePhotoSection(imageUrl = imageUrl, photoUri = photoUri, onTap = { showPhotoSheet = true })

                    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column {
                            Text(
                                text = "Full Name",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                            OutlinedTextField(
                                value = name, onValueChange = { name = it },
                                placeholder = { Text("e.g. Jane Doe", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                singleLine = true
                            )
                        }
                        Column {
                            Text(
                                text = "Bio",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                            OutlinedTextField(
                                value = bio, onValueChange = { bio = it },
                                placeholder = { Text("Tell us about their experience...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                minLines = 3, singleLine = false
                            )
                        }
                        Column {
                            Text(
                                text = "Years of Experience",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                            OutlinedTextField(
                                value = yearsOfExperience, onValueChange = { yearsOfExperience = it },
                                placeholder = { Text("e.g. 5", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AvailabilityLeaveSection(
                        isAvailable = isAvailable,
                        onAvailabilityChange = { isAvailable = it },
                        currentMonth = currentMonth,
                        onMonthChange = { currentMonth = it },
                        leaveDates = leaveDates,
                        rangeStart = rangeStart,
                        rangeEnd = rangeEnd,
                        onDateClick = onDateClick,
                        onAddLeave = {
                            if (rangeStart != null && rangeEnd != null) {
                                expertViewModel.setExpertLeaveRange(expertId, rangeStart.toString(), rangeEnd.toString())
                                rangeStart = null
                                rangeEnd = null
                            }
                        },
                        onRemoveLeave = { start, end ->
                            expertViewModel.deleteExpertLeaveRange(expertId, start.toString(), end.toString())
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove Expert from Team", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }

                val isLoading = updateExpertState is ExpertViewModel.UpdateExpertState.Loading || isUploading
                Button(
                    onClick = {
                        scope.launch {
                            isUploading = true
                            var finalImageUrl = imageUrl
                            if (photoUri != null) {
                                expertViewModel.uploadExpertPhoto(photoUri!!)
                                    .onSuccess { (url, _) -> finalImageUrl = url }
                            }
                            isUploading = false
                            val expert = (expertDetailsState as? ExpertViewModel.ExpertDetailsState.Success)?.expert ?: return@launch
                            val payload = CreateExpertPayload(
                                name = name.trim(), bio = bio.trim().ifEmpty { null },
                                specialization = expert.specialization, contacts = expert.contacts,
                                storeId = storeId, imageUrl = finalImageUrl, isAvailable = isAvailable,
                                availability = expert.availability, noOfAttendedCustomers = expert.noOfAttendedCustomers,
                                ratings = expert.ratings, yearsOfExperience = yearsOfExperience.toIntOrNull()
                            )
                            expertViewModel.updateExpert(expertId, storeId, payload)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(20.dp).height(56.dp),
                    enabled = !isLoading && name.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
        else -> {}
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Expert") },
            text = { Text("Are you sure you want to remove this expert from your team? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false; expertViewModel.deleteExpert(expertId, storeId) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showPhotoSheet) {
        EditExpertPhotoSheet(
            onDismiss = { showPhotoSheet = false },
            onCameraClick = { showPhotoSheet = false; cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
            onGalleryClick = { showPhotoSheet = false; galleryLauncher.launch("image/*") }
        )
    }
}

@Composable
private fun ProfilePhotoSection(imageUrl: String?, photoUri: Uri?, onTap: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.clickable(onClick = onTap)) {
            Box(
                modifier = Modifier.size(112.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(model = photoUri, contentDescription = "Expert photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(model = imageUrl, contentDescription = "Expert photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Change photo", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tap to change photo", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
