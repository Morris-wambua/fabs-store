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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
fun TermsAndConditionsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms and Conditions", fontWeight = FontWeight.Bold) },
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

            TermsSection(
                title = "1. Acceptance of Terms",
                body = "By creating a business account, signing in, listing services, publishing content, processing reservations, or receiving payments through FABS Store, you agree to these Terms and all applicable laws."
            )
            TermsSection(
                title = "2. Platform Role",
                body = "FABS Store provides tools for salon, spa, barbershop, and wellness business operations. You remain responsible for business licensing, regulatory compliance, and service delivery quality."
            )
            TermsSection(
                title = "3. Business Account Responsibilities",
                body = "You must provide accurate business details, maintain secure credentials, and ensure authorized staff use the account. You are responsible for all actions under your business account."
            )
            TermsSection(
                title = "4. Listings, Pricing, and Availability",
                body = "You must keep service descriptions, pricing, durations, and availability accurate. Misleading listings, bait pricing, and deceptive service claims are prohibited."
            )
            TermsSection(
                title = "5. Payments, Settlement, and Fees",
                body = "Payment requests and settlements may use third-party processors and supported currencies. Processor terms, settlement delays, chargebacks, tax obligations, and statutory deductions may apply."
            )
            TermsSection(
                title = "6. Customer Protection and Fair Treatment",
                body = "Businesses must honor valid bookings where applicable, communicate changes promptly, and handle cancellations and refunds in line with law and disclosed policy."
            )
            TermsSection(
                title = "7. Content and Community Rules",
                body = "You may not post unlawful, abusive, discriminatory, infringing, or harmful content. Content uploaded in services, posts, or listings must be related to beauty, wellness, salon, spa, barbershop, or related professional offerings."
            )
            TermsSection(
                title = "8. Prohibited Business Uses",
                body = "You may not use FABS Store for illegal services, trafficking, financial fraud, human exploitation, impersonation, unauthorized data extraction, or security abuse."
            )
            TermsSection(
                title = "9. Privacy and Data Handling",
                body = "FABS and businesses may process sensitive user data including name, phone number, location data, and payment-related data for operations, fraud prevention, support, and compliance. Unauthorized disclosure or misuse of personal data is prohibited."
            )
            TermsSection(
                title = "10. Third-Party Integrations",
                body = "Maps, messaging, authentication, cloud storage, and payment providers are governed by their own terms and privacy policies."
            )
            TermsSection(
                title = "11. Infrastructure and Service Providers",
                body = "FABS may use third-party infrastructure and platform service providers for hosting, deployment, media delivery, security, and performance. Data may be processed through such providers under contractual and security controls."
            )
            TermsSection(
                title = "12. Legal Compliance and Lawful Use",
                body = "Businesses, experts, and customers must comply with national laws, local by-laws, and regulations governing social platforms, online commerce, labor standards, licensing, and consumer protection."
            )
            TermsSection(
                title = "13. Law-Enforcement and Regulatory Requests",
                body = "FABS may share relevant user or transaction data with competent authorities when lawfully requested for legal investigations, and only under applicable legal process in the relevant jurisdiction."
            )
            TermsSection(
                title = "14. Data Deletion Requests",
                body = "Users may request deletion of account and personal data through support channels, subject to legal retention requirements, tax records, anti-fraud obligations, dispute handling, and security audits."
            )
            TermsSection(
                title = "15. Payout and Fund-Hold Policy",
                body = "Eligible business funds may be held and settled on a biweekly cycle, and may be delayed for fraud checks, chargebacks, disputes, compliance reviews, or processor timelines."
            )
            TermsSection(
                title = "16. Refund and Cancellation Policy",
                body = "Customer refunds may be issued where required by law, where a store cancels, where service is materially not delivered, or where duplicate/erroneous charges occur. Partial refunds, credits, and evidence checks may apply."
            )
            TermsSection(
                title = "17. Ratings and Reputation Policy",
                body = "Store, expert, and customer ratings are governed by quality and anti-abuse systems and may be recalculated or adjusted over time. Store ratings may be impacted by inactivity, repeated poor outcomes, or consecutive low customer satisfaction indicators."
            )
            TermsSection(
                title = "18. Intellectual Property",
                body = "FABS platform software, trademarks, and service design remain protected. You retain rights to your lawful business content while granting required operational licenses."
            )
            TermsSection(
                title = "19. Suspension and Termination",
                body = "We may suspend, restrict, or terminate accounts for policy violations, fraud risk, legal directives, safety concerns, or non-compliance with platform obligations."
            )
            TermsSection(
                title = "20. Limitation of Liability",
                body = "To the extent allowed by law, FABS is not liable for indirect, incidental, consequential, or punitive damages arising from use, downtime, or third-party provider failures."
            )
            TermsSection(
                title = "21. Changes to Terms",
                body = "We may update these Terms periodically. Continued use after updates constitutes acceptance of the revised Terms."
            )
            TermsSection(
                title = "22. Contact",
                body = "For legal, compliance, policy, or takedown requests, contact the FABS support/legal channel configured by your organization."
            )

            Divider(modifier = Modifier.padding(top = 4.dp))
            Text(
                text = "These Terms support common platform and store distribution requirements. Obtain legal review for country-specific enforceability and regulated business operations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TermsSection(
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
