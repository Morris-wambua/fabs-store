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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.TransactionType
import com.morrislabs.fabs_store.data.model.WalletTransactionDTO
import com.morrislabs.fabs_store.localization.CurrencyFormatter
import com.morrislabs.fabs_store.localization.LocaleManager

@Composable
fun WalletBalanceCard(
    balance: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val locale = LocaleManager.getActiveLocale(LocalContext.current)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Available Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.formatAmountFromCurrencyCode(balance, currency, locale),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun TransactionTypeIcon(
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (type) {
        TransactionType.ESCROW_RELEASE -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        TransactionType.WITHDRAWAL -> Icons.Default.ArrowDownward to Color(0xFFF44336)
        TransactionType.REFUND -> Icons.Default.Replay to MaterialTheme.colorScheme.tertiary
        TransactionType.TOP_UP -> Icons.Default.Add to MaterialTheme.colorScheme.primary
        TransactionType.WALLET_PAYMENT -> Icons.Default.ArrowDownward to Color(0xFFF44336)
        TransactionType.FX_DEBIT -> Icons.Default.ArrowDownward to Color(0xFFFF9800)
        TransactionType.FX_CREDIT -> Icons.Default.Add to Color(0xFF2196F3)
        TransactionType.COMMISSION -> Icons.Default.ArrowDownward to Color(0xFFF44336)
        TransactionType.PLATFORM_FEE -> Icons.Default.ArrowDownward to Color(0xFFF44336)
        TransactionType.VAT -> Icons.Default.Add to Color(0xFF00796B)
    }

    Surface(
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = tint.copy(alpha = 0.12f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = type.name,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: WalletTransactionDTO,
    walletCurrencyCode: String,
    modifier: Modifier = Modifier
) {
    val locale = LocaleManager.getActiveLocale(LocalContext.current)
    val effectiveCurrency = transaction.currencyCode ?: walletCurrencyCode
    val isCredit = transaction.type == TransactionType.ESCROW_RELEASE ||
            transaction.type == TransactionType.TOP_UP ||
            transaction.type == TransactionType.REFUND ||
            transaction.type == TransactionType.FX_CREDIT ||
            transaction.type == TransactionType.VAT
    val amountPrefix = if (isCredit) "+" else "-"
    val amountColor = if (isCredit) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionTypeIcon(type = transaction.type)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description ?: transaction.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transaction.dateCreated != null) {
                    Text(
                        text = "At ${formatTransactionDate(transaction.dateCreated)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${amountPrefix}${
                        CurrencyFormatter.formatAmountFromCurrencyCode(
                            transaction.amount,
                            effectiveCurrency,
                            locale
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
                Text(
                    text = "Bal: ${
                        CurrencyFormatter.formatAmountFromCurrencyCode(
                            transaction.balanceAfter,
                            effectiveCurrency,
                            locale
                        )
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTransactionDate(dateString: String): String {
    return try {
        val instant = java.time.Instant.parse(dateString)
        val localDateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
        val formatter = java.time.format.DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM)
        localDateTime.format(formatter)
    } catch (e: Exception) {
        dateString
    }
}
