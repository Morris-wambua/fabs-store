package com.morrislabs.fabs_store.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.morrislabs.fabs_store.data.model.WalletDTO
import com.morrislabs.fabs_store.localization.CurrencyFormatter
import com.morrislabs.fabs_store.localization.LocaleManager
import com.morrislabs.fabs_store.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyExchangeBottomSheet(
    wallets: List<WalletDTO>,
    sourceWallet: WalletDTO,
    exchangeState: WalletViewModel.ExchangeState,
    previewState: WalletViewModel.ExchangeState,
    onPreview: (sourceCurrency: String, targetCurrency: String, amount: Double) -> Unit,
    onExchange: (sourceCurrency: String, targetCurrency: String, amount: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val locale = LocaleManager.getActiveLocale(LocalContext.current)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val otherWallets = wallets.filter { it.currency != sourceWallet.currency }
    var targetCurrency by rememberSaveable { mutableStateOf(otherWallets.firstOrNull()?.currency ?: "") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val isLoading = exchangeState is WalletViewModel.ExchangeState.Loading

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Convert Currency",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "From: ${sourceWallet.currency} (${CurrencyFormatter.formatAmountFromCurrencyCode(sourceWallet.balance, sourceWallet.currency, locale)})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text("To Currency", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = targetCurrency,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    otherWallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = { Text("${wallet.currency} (${CurrencyFormatter.formatAmountFromCurrencyCode(wallet.balance, wallet.currency, locale)})") },
                            onClick = {
                                targetCurrency = wallet.currency
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    amountError = null
                },
                label = { Text("Amount (${sourceWallet.currency})") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it) } },
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        amountError = "Enter a valid amount"
                    } else if (amount > sourceWallet.balance) {
                        amountError = "Exceeds available balance"
                    } else {
                        onPreview(sourceWallet.currency, targetCurrency, amount)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && targetCurrency.isNotEmpty()
            ) {
                Text("Preview Rate")
            }

            when (previewState) {
                is WalletViewModel.ExchangeState.Loading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                is WalletViewModel.ExchangeState.Success -> {
                    val preview = previewState.result
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Rate", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "1 ${preview.sourceCurrencyCode} = ${String.format("%.4f", preview.exchangeRate)} ${preview.targetCurrencyCode}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Fee (1%)", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    CurrencyFormatter.formatAmountFromCurrencyCode(preview.spreadFee, preview.sourceCurrencyCode, locale),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            HorizontalDivider()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("You receive", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    CurrencyFormatter.formatAmountFromCurrencyCode(preview.targetAmount, preview.targetCurrencyCode, locale),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                is WalletViewModel.ExchangeState.Error -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(previewState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        amountError = "Enter a valid amount"
                    } else if (amount > sourceWallet.balance) {
                        amountError = "Exceeds available balance"
                    } else {
                        onExchange(sourceWallet.currency, targetCurrency, amount)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading && previewState is WalletViewModel.ExchangeState.Success && targetCurrency.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Convert", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
