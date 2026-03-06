package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Last Updated: March 6, 2026",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PrivacySection(
                title = "1. Data We Collect",
                body = "We collect personal and sensitive data including account identity details, business profile information, phone numbers, location data, staff/customer records, reservations, and payment-related information."
            )
            PrivacySection(
                title = "2. Purpose of Processing",
                body = "Data is processed to run store operations, reservations, expert scheduling, payments and settlements, fraud checks, customer support, analytics, and legal compliance."
            )
            PrivacySection(
                title = "3. Data Sharing",
                body = "Data may be shared with payment processors, messaging providers, authentication providers, and infrastructure/platform service providers for hosting, security, media delivery, and deployment operations."
            )
            PrivacySection(
                title = "4. Legal Requests",
                body = "FABS may disclose relevant data to competent authorities when legally requested and under applicable country legal procedures."
            )
            PrivacySection(
                title = "5. Data Retention and Deletion",
                body = "Users may request deletion of account and personal data through support channels. Legal and compliance retention periods may still apply for audits, taxes, disputes, and fraud prevention."
            )
            PrivacySection(
                title = "6. Security",
                body = "We apply technical and organizational safeguards to protect data confidentiality, integrity, and availability, while recognizing that no system offers absolute security."
            )
            PrivacySection(
                title = "7. Cross-Border Processing",
                body = "Data may be processed across regions by approved service providers under contractual and legal safeguards."
            )
            PrivacySection(
                title = "8. Your Rights",
                body = "Depending on jurisdiction, users may request access, correction, deletion, restriction, objection, or portability of personal data."
            )
            PrivacySection(
                title = "9. Contact",
                body = "For privacy rights requests, data access, and complaints, contact the FABS support/privacy channel configured by your organization."
            )

            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
            Text(
                text = "This policy is a baseline privacy statement and should be reviewed by legal counsel for jurisdiction-specific obligations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
