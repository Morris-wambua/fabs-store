package com.morrislabs.fabs_store.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.localization.LocaleManager
import com.morrislabs.fabs_store.ui.viewmodel.AuthViewModel
import java.util.Locale

private data class LanguageOption(
    val code: String,
    val label: String
)

private val languageOptions = listOf(
    LanguageOption(code = "en", label = "English"),
    LanguageOption(code = "zh", label = "Chinese"),
    LanguageOption(code = "ja", label = "Japanese"),
    LanguageOption(code = "fr", label = "French"),
    LanguageOption(code = "de", label = "German")
)

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStoreProfile: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val activeLocale = LocaleManager.getActiveLocale(context)
    val initialLanguage = languageOptions.firstOrNull { it.code == activeLocale.language }?.code ?: "en"
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "App")

            SettingItem(
                icon = Icons.Default.Edit,
                title = "Edit Store Profile",
                subtitle = "Update store name, logo, and details",
                onClick = onNavigateToStoreProfile
            )

            SettingItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage notification preferences",
                onClick = { }
            )

            SettingItem(
                icon = Icons.Default.Security,
                title = "Privacy & Security",
                subtitle = "Password, permissions, security",
                onClick = { }
            )

            SettingItem(
                icon = Icons.Default.Language,
                title = "App Language",
                subtitle = languageOptions.firstOrNull { it.code == selectedLanguage }?.label ?: "English",
                onClick = { showLanguageDialog = true }
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            SettingsSection(title = "Account")

            SettingItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App version 1.0.0",
                onClick = { }
            )

            SettingItem(
                icon = Icons.Default.Info,
                title = "Terms and Conditions",
                subtitle = "Review legal terms for app usage",
                onClick = onNavigateToTerms
            )

            SettingItem(
                icon = Icons.Default.Info,
                title = "Privacy Policy",
                subtitle = "Review how your data is handled",
                onClick = onNavigateToPrivacy
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            SettingItem(
                icon = Icons.Default.Logout,
                title = "Logout",
                subtitle = "Sign out of your account",
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                isDestructive = true
            )

            Box(modifier = Modifier.height(24.dp))
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select App Language") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    languageOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLanguage = option.code }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == option.code,
                                onClick = { selectedLanguage = option.code }
                            )
                            Text(
                                text = option.label,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val country = activeLocale.country.ifBlank { Locale.getDefault().country }
                        val languageTag = if (country.isBlank()) {
                            selectedLanguage
                        } else {
                            "$selectedLanguage-$country"
                        }
                        LocaleManager.setLanguageOverride(context, languageTag)
                        showLanguageDialog = false
                        (context as? Activity)?.recreate()
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
