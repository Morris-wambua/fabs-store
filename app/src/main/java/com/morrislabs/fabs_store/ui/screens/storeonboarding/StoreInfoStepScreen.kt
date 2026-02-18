package com.morrislabs.fabs_store.ui.screens.storeonboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.morrislabs.fabs_store.ui.viewmodel.CreateStoreWizardViewModel
import java.io.File

@Composable
fun StoreInfoStepScreen(
    wizardViewModel: CreateStoreWizardViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val state by wizardViewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 120.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopBarSection(onNavigateBack = onNavigateBack)
            Spacer(modifier = Modifier.height(24.dp))
            LogoUploadSection(
                logoUri = state.storeLogoUri,
                onLogoSelected = wizardViewModel::updateStoreLogoUri
            )
            Spacer(modifier = Modifier.height(28.dp))
            FormFieldsSection(
                storeName = state.storeName,
                onStoreNameChange = wizardViewModel::updateStoreName,
                storeHandle = state.storeHandle,
                onStoreHandleChange = wizardViewModel::updateStoreHandle,
                countryCode = state.countryCode,
                onCountryCodeChange = wizardViewModel::updateCountryCode,
                contactNumber = state.contactNumber,
                onContactNumberChange = wizardViewModel::updateContactNumber,
                aboutStore = state.aboutStore,
                onAboutStoreChange = wizardViewModel::updateAboutStore
            )
        }

        FooterSection(
            modifier = Modifier.align(Alignment.BottomCenter),
            onRegisterClick = {
                if (wizardViewModel.canProceedFromStep1()) {
                    onNavigateNext()
                }
            }
        )
    }
}

@Composable
private fun TopBarSection(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Create Your Store",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Store Registration Progress",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Text(
                text = "1 of 3",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { 0.33f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun LogoUploadSection(
    logoUri: Uri?,
    onLogoSelected: (Uri?) -> Unit
) {
    val context = LocalContext.current
    var showImagePickerSheet by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { onLogoSelected(it) } }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraImageUri?.let { onLogoSelected(it) } }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "store_logo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.clickable { showImagePickerSheet = true }
        ) {
            Box(
                modifier = Modifier.size(128.dp),
                contentAlignment = Alignment.Center
            ) {
                if (logoUri != null) {
                    AsyncImage(
                        model = logoUri,
                        contentDescription = "Store logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.05f),
                            radius = size.minDimension / 2
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.4f),
                            radius = size.minDimension / 2,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(10f, 10f), 0f
                                )
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            FloatingActionButton(
                onClick = { showImagePickerSheet = true },
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Store Logo",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = "Tap to add your brand identity",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }

    if (showImagePickerSheet) {
        ImagePickerBottomSheet(
            onDismiss = { showImagePickerSheet = false },
            onCameraClick = {
                showImagePickerSheet = false
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            },
            onGalleryClick = {
                showImagePickerSheet = false
                galleryLauncher.launch("image/*")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePickerBottomSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Store Logo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .clickable(onClick = onCameraClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Take a Photo",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Use your camera to capture the logo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .clickable(onClick = onGalleryClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Choose from Gallery",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Select an existing image from your device",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class CountryCodeEntry(
    val flag: String,
    val code: String,
    val name: String
)

private val countryCodes = listOf(
    CountryCodeEntry("🇰🇪", "+254", "Kenya"),
    CountryCodeEntry("🇺🇸", "+1", "United States"),
    CountryCodeEntry("🇬🇧", "+44", "United Kingdom"),
    CountryCodeEntry("🇳🇬", "+234", "Nigeria"),
    CountryCodeEntry("🇿🇦", "+27", "South Africa"),
    CountryCodeEntry("🇹🇿", "+255", "Tanzania"),
    CountryCodeEntry("🇺🇬", "+256", "Uganda"),
    CountryCodeEntry("🇪🇹", "+251", "Ethiopia"),
    CountryCodeEntry("🇬🇭", "+233", "Ghana"),
    CountryCodeEntry("🇷🇼", "+250", "Rwanda"),
    CountryCodeEntry("🇮🇳", "+91", "India"),
    CountryCodeEntry("🇦🇪", "+971", "UAE"),
    CountryCodeEntry("🇨🇦", "+1", "Canada"),
    CountryCodeEntry("🇦🇺", "+61", "Australia"),
    CountryCodeEntry("🇩🇪", "+49", "Germany"),
    CountryCodeEntry("🇫🇷", "+33", "France"),
    CountryCodeEntry("🇧🇷", "+55", "Brazil"),
    CountryCodeEntry("🇨🇳", "+86", "China"),
    CountryCodeEntry("🇯🇵", "+81", "Japan"),
    CountryCodeEntry("🇪🇬", "+20", "Egypt")
)

@Composable
private fun FormFieldsSection(
    storeName: String,
    onStoreNameChange: (String) -> Unit,
    storeHandle: String,
    onStoreHandleChange: (String) -> Unit,
    countryCode: String,
    onCountryCodeChange: (String) -> Unit,
    contactNumber: String,
    onContactNumberChange: (String) -> Unit,
    aboutStore: String,
    onAboutStoreChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LabeledTextField(
            label = "Store Name",
            value = storeName,
            onValueChange = onStoreNameChange,
            placeholder = "e.g. Fabs Fashion Hub",
            trailingIcon = Icons.Default.Storefront
        )

        LabeledTextField(
            label = "Unique Display Name (@handle)",
            value = storeHandle,
            onValueChange = onStoreHandleChange,
            placeholder = "cool_fashion_hub",
            trailingIcon = Icons.Default.AlternateEmail,
            leadingPrefix = "@"
        )

        PhoneNumberField(
            countryCode = countryCode,
            onCountryCodeChange = onCountryCodeChange,
            phoneNumber = contactNumber,
            onPhoneNumberChange = onContactNumberChange
        )

        LabeledTextField(
            label = "About the Store",
            value = aboutStore,
            onValueChange = onAboutStoreChange,
            placeholder = "Describe your store, products, and vision...",
            trailingIcon = Icons.Default.Description,
            singleLine = false,
            minLines = 4
        )
    }
}

@Composable
private fun PhoneNumberField(
    countryCode: String,
    onCountryCodeChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedEntry = countryCodes.find { it.code == countryCode } ?: countryCodes.first()

    Column {
        Text(
            text = "Contact Number",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Country code selector
            Box {
                OutlinedTextField(
                    value = "${selectedEntry.flag} ${selectedEntry.code}",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .width(120.dp)
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        disabledTextColor = MaterialTheme.colorScheme.onBackground,
                        disabledBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select country",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { expanded = true }
                        )
                    },
                    singleLine = true
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(260.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    countryCodes.forEach { entry ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${entry.flag}  ${entry.name} (${entry.code})",
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                onCountryCodeChange(entry.code)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Phone number input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                placeholder = {
                    Text(
                        text = "712 345 678",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: ImageVector,
    leadingPrefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            },
            leadingIcon = if (leadingPrefix != null) {
                {
                    Text(
                        text = leadingPrefix,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            } else null,
            trailingIcon = {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            minLines = minLines
        )
    }
}

@Composable
private fun FooterSection(
    modifier: Modifier = Modifier,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Register Store",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                append("By clicking Register, you agree to our ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Terms of Service") }
                append(" and ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Privacy Policy") }
                append(".")
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
