package com.morrislabs.fabs_store.data.model

import kotlinx.serialization.Serializable
import java.util.Currency
import java.util.Locale

@Serializable
enum class WalletType {
    CUSTOMER, STORE
}

@Serializable
enum class TransactionType {
    TOP_UP, REFUND, ESCROW_RELEASE, WITHDRAWAL, WALLET_PAYMENT, FX_DEBIT, FX_CREDIT
}

@Serializable
data class WalletDTO(
    val id: String? = null,
    val walletType: WalletType = WalletType.STORE,
    val ownerId: String = "",
    val balance: Double = 0.0,
    val currency: String = Currency.getInstance(Locale.getDefault()).currencyCode
)

@Serializable
data class WalletTransactionDTO(
    val id: String? = null,
    val type: TransactionType = TransactionType.ESCROW_RELEASE,
    val amount: Double = 0.0,
    val balanceBefore: Double = 0.0,
    val balanceAfter: Double = 0.0,
    val referenceId: String? = null,
    val description: String? = null,
    val dateCreated: String? = null
)

@Serializable
data class PagedWalletTransactionResponse(
    val content: List<WalletTransactionDTO> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class WithdrawRequest(
    val phoneNumber: String? = null,
    val amount: Double,
    val disbursementMethod: String = "MPESA",
    val stripeConnectedAccountId: String? = null,
    val currencyCode: String? = null
)

@Serializable
data class PayoutRequestPayload(
    val amount: Double,
    val currencyCode: String,
    val payoutDestination: String? = null,
    val disbursementMethod: String = "MPESA",
    val stripeConnectedAccountId: String? = null
)

@Serializable
data class PayoutResponseDTO(
    val id: String? = null,
    val storeId: String? = null,
    val currencyCode: String = "",
    val amount: Double = 0.0,
    val status: String = "PENDING",
    val payoutDestination: String? = null,
    val disbursementMethod: String? = null,
    val createdAt: String? = null,
    val processedAt: String? = null,
    val failureReason: String? = null
)

@Serializable
data class PagedPayoutResponse(
    val content: List<PayoutResponseDTO> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class CurrencyExchangeRequest(
    val sourceCurrencyCode: String,
    val targetCurrencyCode: String,
    val amount: Double
)

@Serializable
data class CurrencyExchangeResponse(
    val sourceCurrencyCode: String = "",
    val targetCurrencyCode: String = "",
    val sourceAmount: Double = 0.0,
    val exchangeRate: Double = 0.0,
    val spreadFee: Double = 0.0,
    val targetAmount: Double = 0.0,
    val sourceBalanceAfter: Double = 0.0,
    val targetBalanceAfter: Double = 0.0
)


