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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.morrislabs.fabs_store.data.model.FetchStoreResponse
import com.morrislabs.fabs_store.data.model.UpdateStorePayload
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun StoreProfileEditorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditLocation: () -> Unit,
    onNavigateToBusinessHours: () -> Unit,
    storeViewModel: StoreViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val updateState by storeViewModel.updateStoreState.collectAsState()

    var initialized by remember { mutableStateOf(false) }
    var storeSnapshot by remember { mutableStateOf<FetchStoreResponse?>(null) }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("+254") }
    var localPhoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var saveRequested by remember { mutableStateOf(false) }
    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCoverUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedLogoUrl by remember { mutableStateOf<String?>(null) }
    var uploadedLogoS3Key by remember { mutableStateOf<String?>(null) }
    var uploadedCoverUrl by remember { mutableStateOf<String?>(null) }
    var uploadedCoverS3Key by remember { mutableStateOf<String?>(null) }
    var isUploadingLogo by remember { mutableStateOf(false) }
    var isUploadingCover by remember { mutableStateOf(false) }
    var uploadTarget by remember { mutableStateOf<StoreImageTarget?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val target = uploadTarget
        if (uri == null || target == null) {
            return@rememberLauncherForActivityResult
        }

        when (target) {
            StoreImageTarget.LOGO -> {
                selectedLogoUri = uri
                isUploadingLogo = true
            }
            StoreImageTarget.COVER -> {
                selectedCoverUri = uri
                isUploadingCover = true
            }
        }

        storeViewModel.uploadStoreImage(
            uri = uri,
            onSuccess = { url, key ->
                when (target) {
                    StoreImageTarget.LOGO -> {
                        uploadedLogoUrl = url
                        uploadedLogoS3Key = key
                        isUploadingLogo = false
                    }
                    StoreImageTarget.COVER -> {
                        uploadedCoverUrl = url
                        uploadedCoverS3Key = key
                        isUploadingCover = false
                    }
                }
                uploadTarget = null
            },
            onFailure = {
                when (target) {
                    StoreImageTarget.LOGO -> {
                        selectedLogoUri = null
                        isUploadingLogo = false
                    }
                    StoreImageTarget.COVER -> {
                        selectedCoverUri = null
                        isUploadingCover = false
                    }
                }
                uploadTarget = null
            }
        )
    }

    LaunchedEffect(Unit) {
        storeViewModel.fetchUserStore()
    }

    LaunchedEffect(storeState) {
        if (storeState is StoreViewModel.StoreState.Success) {
            val store = (storeState as StoreViewModel.StoreState.Success).data
            storeSnapshot = store
            if (!initialized) {
                name = store.name
                username = store.username
                about = store.about.orEmpty()
                val phoneParts = parsePhoneForUi(store.phone.orEmpty())
                selectedCountryCode = phoneParts.first
                localPhoneNumber = phoneParts.second
                email = store.email.orEmpty()
                initialized = true
            }
        }
    }

    LaunchedEffect(updateState, saveRequested) {
        if (!saveRequested) {
            return@LaunchedEffect
        }
        when (updateState) {
            is StoreViewModel.UpdateStoreState.Success -> {
                saveRequested = false
                storeViewModel.resetUpdateStoreState()
            }
            is StoreViewModel.UpdateStoreState.Error -> {
                saveRequested = false
                storeViewModel.resetUpdateStoreState()
            }
            else -> Unit
        }
    }

    val isSaving = updateState is StoreViewModel.UpdateStoreState.Loading
    val isUploadingAny = isUploadingLogo || isUploadingCover

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StoreProfileTopBar(onNavigateBack = onNavigateBack)

            when (val state = storeState) {
                is StoreViewModel.StoreState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is StoreViewModel.StoreState.Success -> {
                    val store = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        StoreHeaderSection(
                            store = store,
                            logoImage = selectedLogoUri ?: uploadedLogoUrl ?: store.logoUrl,
                            coverImage = selectedCoverUri ?: uploadedCoverUrl ?: store.coverUrl ?: store.logoUrl,
                            isUploadingLogo = isUploadingLogo,
                            isUploadingCover = isUploadingCover,
                            onEditLogo = {
                                uploadTarget = StoreImageTarget.LOGO
                                galleryLauncher.launch("image/*")
                            },
                            onEditCover = {
                                uploadTarget = StoreImageTarget.COVER
                                galleryLauncher.launch("image/*")
                            }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 20.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SectionLabel("BASIC INFORMATION")
                            FieldBlock("Store Name", name) { name = it }
                            FieldBlock("Display Name", username) { username = it }
                            FieldBlock("Description", about, minLines = 4) { about = it }

                            Spacer(modifier = Modifier.height(4.dp))
                            SectionLabel("CONTACT & LOCATION")
                            ActionRow(
                                title = "Location/Address",
                                subtitle = store.locationDTO?.description ?: store.locationDTO?.name ?: "Set location",
                                icon = Icons.Default.LocationOn,
                                onClick = onNavigateToEditLocation
                            )
                            PhoneFieldBlock(
                                label = "Contact Phone",
                                countryCode = selectedCountryCode,
                                localPhone = localPhoneNumber,
                                onCountryCodeChange = { selectedCountryCode = it },
                                onLocalPhoneChange = { localPhoneNumber = it }
                            )
                            FieldBlock("Business Email", email) { email = it }

                            Spacer(modifier = Modifier.height(4.dp))
                            SectionLabel("OPERATIONAL INFO")
                            ActionRow(
                                title = "Business Hours",
                                subtitle = store.businessHours?.let { summarizeHours(it) } ?: "Set weekly hours",
                                icon = Icons.Default.Schedule,
                                onClick = onNavigateToBusinessHours
                            )
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Unable to load store profile")
                    }
                }
            }
        }

        SaveFooter(
            modifier = Modifier.align(Alignment.BottomCenter),
            isSaving = isSaving || isUploadingAny,
            onSave = {
                val store = storeSnapshot ?: return@SaveFooter
                val storeId = store.id ?: return@SaveFooter
                saveRequested = true
                storeViewModel.updateStore(
                    storeId = storeId,
                    payload = UpdateStorePayload(
                        name = name.trim(),
                        username = username.trim(),
                        description = about.trim(),
                        about = about.trim(),
                        phone = formatPhoneForSave(selectedCountryCode, localPhoneNumber),
                        email = email.trim().ifBlank { null },
                        logoUrl = uploadedLogoUrl,
                        logoS3Key = uploadedLogoS3Key,
                        coverUrl = uploadedCoverUrl,
                        coverS3Key = uploadedCoverS3Key
                    )
                )
            }
        )
    }
}

@Composable
private fun StoreProfileTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Store Profile",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Cancel",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
private fun StoreHeaderSection(
    store: FetchStoreResponse,
    logoImage: Any?,
    coverImage: Any?,
    isUploadingLogo: Boolean,
    isUploadingCover: Boolean,
    onEditLogo: () -> Unit,
    onEditCover: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
            ) {
                AsyncImage(
                    model = coverImage,
                    contentDescription = "Store cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp)
                        .clickable(enabled = !isUploadingCover, onClick = onEditCover),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isUploadingCover) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "Edit Cover",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 108.dp)
                    .size(126.dp)
                    .shadow(10.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.background, CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = logoImage,
                        contentDescription = "Store logo",
                        modifier = Modifier
                            .size(54.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .clickable(enabled = !isUploadingLogo, onClick = onEditLogo),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 2.dp,
                    tonalElevation = 0.dp,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.background)
                ) {
                    if (isUploadingLogo) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit logo",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(store.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(store.username, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FieldBlock(
    label: String,
    value: String,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
private fun PhoneFieldBlock(
    label: String,
    countryCode: String,
    localPhone: String,
    onCountryCodeChange: (String) -> Unit,
    onLocalPhoneChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedEntry = countryCodes.find { it.code == countryCode } ?: countryCodes.first()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                OutlinedTextField(
                    value = "${selectedEntry.flag} ${selectedEntry.code}",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
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
                    onDismissRequest = { expanded = false }
                ) {
                    countryCodes.forEach { entry ->
                        DropdownMenuItem(
                            text = {
                                Text("${entry.flag}  ${entry.name} (${entry.code})")
                            },
                            onClick = {
                                onCountryCodeChange(entry.code)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = localPhone,
                onValueChange = onLocalPhoneChange,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("712 345 678") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(7.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SaveFooter(
    modifier: Modifier = Modifier,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Changes", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

private enum class StoreImageTarget {
    LOGO,
    COVER
}

private data class CountryCodeEntry(
    val flag: String,
    val code: String,
    val name: String
)

private val countryCodes = listOf(
    CountryCodeEntry("\uD83C\uDDF0\uD83C\uDDEA", "+254", "Kenya"),
    CountryCodeEntry("\uD83C\uDDFA\uD83C\uDDF8", "+1", "United States"),
    CountryCodeEntry("\uD83C\uDDEC\uD83C\uDDE7", "+44", "United Kingdom"),
    CountryCodeEntry("\uD83C\uDDF3\uD83C\uDDEC", "+234", "Nigeria"),
    CountryCodeEntry("\uD83C\uDDFF\uD83C\uDDE6", "+27", "South Africa"),
    CountryCodeEntry("\uD83C\uDDF9\uD83C\uDDFF", "+255", "Tanzania"),
    CountryCodeEntry("\uD83C\uDDFA\uD83C\uDDEC", "+256", "Uganda"),
    CountryCodeEntry("\uD83C\uDDEA\uD83C\uDDF9", "+251", "Ethiopia"),
    CountryCodeEntry("\uD83C\uDDEC\uD83C\uDDED", "+233", "Ghana"),
    CountryCodeEntry("\uD83C\uDDF7\uD83C\uDDFC", "+250", "Rwanda"),
    CountryCodeEntry("\uD83C\uDDEE\uD83C\uDDF3", "+91", "India"),
    CountryCodeEntry("\uD83C\uDDE6\uD83C\uDDEA", "+971", "UAE"),
    CountryCodeEntry("\uD83C\uDDE8\uD83C\uDDE6", "+1", "Canada"),
    CountryCodeEntry("\uD83C\uDDE6\uD83C\uDDFA", "+61", "Australia"),
    CountryCodeEntry("\uD83C\uDDE9\uD83C\uDDEA", "+49", "Germany"),
    CountryCodeEntry("\uD83C\uDDEB\uD83C\uDDF7", "+33", "France"),
    CountryCodeEntry("\uD83C\uDDE7\uD83C\uDDF7", "+55", "Brazil"),
    CountryCodeEntry("\uD83C\uDDE8\uD83C\uDDF3", "+86", "China"),
    CountryCodeEntry("\uD83C\uDDEF\uD83C\uDDF5", "+81", "Japan"),
    CountryCodeEntry("\uD83C\uDDEA\uD83C\uDDEC", "+20", "Egypt")
)

private fun parsePhoneForUi(phone: String): Pair<String, String> {
    if (phone.isBlank()) return "+254" to ""
    val matched = countryCodes.firstOrNull { phone.startsWith(it.code) }
    return if (matched != null) {
        matched.code to phone.removePrefix(matched.code).trimStart()
    } else {
        "+254" to phone
    }
}

private fun formatPhoneForSave(countryCode: String, localPhone: String): String? {
    val local = localPhone.trim()
    if (local.isBlank()) return null
    return "$countryCode$local"
}

private fun summarizeHours(hours: List<com.morrislabs.fabs_store.data.model.BusinessHourDTO>): String {
    val openDays = hours.filter { it.isOpen }
    if (openDays.isEmpty()) {
        return "Closed all week"
    }
    val first = openDays.first()
    return "${openDays.size} days open, starts ${first.openTime ?: "--"}"
}
