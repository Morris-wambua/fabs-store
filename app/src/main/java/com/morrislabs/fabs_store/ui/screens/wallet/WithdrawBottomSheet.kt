package com.morrislabs.fabs_store.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morrislabs.fabs_store.localization.CurrencyFormatter
import com.morrislabs.fabs_store.localization.LocaleManager
import com.morrislabs.fabs_store.localization.PhoneNumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawBottomSheet(
    balance: Double,
    currency: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onWithdraw: (
        amount: Double,
        disbursementMethod: String,
        phoneNumber: String?,
        stripeConnectedAccountId: String?
    ) -> Unit
) {
    val locale = LocaleManager.getActiveLocale(LocalContext.current)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMethod by rememberSaveable { mutableStateOf("MPESA") }
    var countryCode by rememberSaveable { mutableStateOf(PhoneNumberFormatter.defaultCallingCode(locale)) }
    var expanded by remember { mutableStateOf(false) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var stripeConnectedAccountId by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var phoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var stripeAccountError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Withdraw Funds",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Available: ${CurrencyFormatter.formatWithCurrencyCode(balance, currency, locale)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Disbursement Method",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                FilterChip(
                    selected = selectedMethod == "MPESA",
                    onClick = {
                        selectedMethod = "MPESA"
                        stripeAccountError = null
                    },
                    label = { Text("M-Pesa") },
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = selectedMethod == "STRIPE",
                    onClick = {
                        selectedMethod = "STRIPE"
                        phoneError = null
                    },
                    label = { Text("Stripe") },
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedMethod == "MPESA") {
                Text(
                    text = "M-Pesa Phone Number",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val defaultCode = PhoneNumberFormatter.defaultCallingCode(locale)
                val selectedEntry = countryCodes.find { it.code == countryCode }
                    ?: countryCodes.find { it.code == defaultCode }
                    ?: countryCodes.first()

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
                            singleLine = true,
                            enabled = !isLoading
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
                                        countryCode = entry.code
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.filter { it.isDigit() }
                            if (digitsOnly.length <= maxLocalDigits(countryCode)) {
                                phoneNumber = digitsOnly
                                phoneError = null
                            }
                        },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it) } },
                        singleLine = true,
                        enabled = !isLoading
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            } else {
                OutlinedTextField(
                    value = stripeConnectedAccountId,
                    onValueChange = {
                        stripeConnectedAccountId = it
                        stripeAccountError = null
                    },
                    label = { Text("Stripe Connected Account ID") },
                    placeholder = { Text("acct_...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = stripeAccountError != null,
                    supportingText = stripeAccountError?.let { { Text(it) } },
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    amountError = null
                },
                label = { Text("Amount ($currency)") },
                placeholder = { Text("Enter amount") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it) } },
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    var hasError = false

                    if (selectedMethod == "MPESA") {
                        if (phoneNumber.isBlank()) {
                            phoneError = "Phone number is required"
                            hasError = true
                        }
                    } else {
                        if (stripeConnectedAccountId.isBlank()) {
                            stripeAccountError = "Stripe connected account ID is required"
                            hasError = true
                        }
                    }

                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        amountError = "Enter a valid amount"
                        hasError = true
                    } else if (amount > balance) {
                        amountError = "Amount exceeds available balance"
                        hasError = true
                    }

                    if (!hasError && amount != null) {
                        onWithdraw(
                            amount,
                            selectedMethod,
                            if (selectedMethod == "MPESA") "$countryCode$phoneNumber" else null,
                            if (selectedMethod == "STRIPE") stripeConnectedAccountId else null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (selectedMethod == "MPESA") "Withdraw via M-Pesa" else "Withdraw via Stripe",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

private fun maxLocalDigits(countryCode: String): Int = when (countryCode) {
    "+254" -> 9
    "+255" -> 9
    "+256" -> 9
    "+251" -> 9
    "+250" -> 9
    "+233" -> 9
    "+234" -> 10
    "+27" -> 9
    "+91" -> 10
    "+1" -> 10
    "+44" -> 10
    "+971" -> 9
    "+61" -> 9
    "+49" -> 11
    "+33" -> 9
    "+55" -> 11
    "+86" -> 11
    "+81" -> 10
    "+20" -> 10
    else -> 12
}

private data class CountryCodeEntry(val flag: String, val code: String, val name: String)

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
