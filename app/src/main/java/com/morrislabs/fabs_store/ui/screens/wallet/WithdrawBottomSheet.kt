package com.morrislabs.fabs_store.ui.screens.wallet

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMethod by rememberSaveable { mutableStateOf("MPESA") }
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
                text = "Available: $currency ${String.format("%,.2f", balance)}",
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
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        phoneError = null
                    },
                    label = { Text("M-Pesa Phone Number") },
                    placeholder = { Text("e.g. 2547XXXXXXXX") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it) } },
                    singleLine = true,
                    enabled = !isLoading
                )

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
                            if (selectedMethod == "MPESA") phoneNumber else null,
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
