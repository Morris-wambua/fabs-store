package com.morrislabs.fabs_store.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.CreateExpertPayload
import com.morrislabs.fabs_store.data.model.SubCategory
import com.morrislabs.fabs_store.ui.viewmodel.ExpertViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CreateExpertScreen(
    storeId: String,
    onNavigateBack: () -> Unit,
    onExpertCreated: () -> Unit,
    expertViewModel: ExpertViewModel = viewModel()
) {
    val createExpertState by expertViewModel.createExpertState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var yearsOfExperience by rememberSaveable { mutableStateOf("") }
    var selectedSkills by remember { mutableStateOf<List<SubCategory>>(emptyList()) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
        onDispose { expertViewModel.resetCreateExpertState() }
    }

    val isLoading = createExpertState is ExpertViewModel.CreateExpertState.Loading || isUploading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Add Expert",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            CreateExpertForm(
                name = name,
                onNameChange = { name = it },
                bio = bio,
                onBioChange = { bio = it },
                yearsOfExperience = yearsOfExperience,
                onYearsOfExperienceChange = { yearsOfExperience = it },
                photoUri = photoUri,
                selectedSkills = selectedSkills,
                allSkills = SubCategory.entries,
                onSkillToggle = { skill ->
                    selectedSkills = if (selectedSkills.contains(skill)) {
                        selectedSkills - skill
                    } else {
                        selectedSkills + skill
                    }
                },
                onCameraClick = {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                },
                onGalleryClick = {
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    isUploading = true
                    var imageUrl: String? = null
                    if (photoUri != null) {
                        expertViewModel.uploadExpertPhoto(photoUri!!)
                            .onSuccess { (url, _) -> imageUrl = url }
                    }
                    isUploading = false
                    val payload = CreateExpertPayload(
                        name = name.trim(),
                        bio = bio.trim().ifEmpty { null },
                        specialization = selectedSkills,
                        contacts = "",
                        storeId = storeId,
                        imageUrl = imageUrl,
                        isAvailable = true,
                        yearsOfExperience = yearsOfExperience.toIntOrNull()
                    )
                    expertViewModel.createExpert(storeId, payload)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(20.dp)
                .height(56.dp),
            enabled = !isLoading && name.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Add Expert", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.size(8.dp))
                Icon(Icons.Default.PersonAdd, contentDescription = null)
            }
        }
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
}
