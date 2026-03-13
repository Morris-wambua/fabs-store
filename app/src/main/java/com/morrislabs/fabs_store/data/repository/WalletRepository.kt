package com.morrislabs.fabs_store.data.repository

import android.content.Context
import com.morrislabs.fabs_store.data.api.WalletApiService
import com.morrislabs.fabs_store.data.model.CurrencyExchangeRequest
import com.morrislabs.fabs_store.data.model.CurrencyExchangeResponse
import com.morrislabs.fabs_store.data.model.PagedPayoutResponse
import com.morrislabs.fabs_store.data.model.PagedWalletTransactionResponse
import com.morrislabs.fabs_store.data.model.PayoutRequestPayload
import com.morrislabs.fabs_store.data.model.PayoutResponseDTO
import com.morrislabs.fabs_store.data.model.WalletDTO
import com.morrislabs.fabs_store.data.model.WithdrawRequest
import com.morrislabs.fabs_store.util.TokenManager

class WalletRepository(context: Context, tokenManager: TokenManager) {
    private val walletApiService = WalletApiService(context, tokenManager)

    suspend fun fetchStoreWallet(storeId: String): Result<WalletDTO> =
        walletApiService.fetchStoreWallet(storeId)

    suspend fun fetchAllStoreWallets(storeId: String): Result<List<WalletDTO>> =
        walletApiService.fetchAllStoreWallets(storeId)

    suspend fun fetchStoreTransactions(
        storeId: String,
        page: Int = 0,
        size: Int = 20
    ): Result<PagedWalletTransactionResponse> =
        walletApiService.fetchStoreTransactions(storeId, page, size)

    suspend fun initiateWithdrawal(storeId: String, request: WithdrawRequest): Result<WalletDTO> =
        walletApiService.initiateWithdrawal(storeId, request)

    suspend fun requestPayout(storeId: String, request: PayoutRequestPayload): Result<PayoutResponseDTO> =
        walletApiService.requestPayout(storeId, request)

    suspend fun fetchPayouts(storeId: String, page: Int = 0, size: Int = 20): Result<PagedPayoutResponse> =
        walletApiService.fetchPayouts(storeId, page, size)

    suspend fun exchangeCurrency(storeId: String, request: CurrencyExchangeRequest): Result<CurrencyExchangeResponse> =
        walletApiService.exchangeCurrency(storeId, request)

    suspend fun fetchWalletTransactions(walletId: String, page: Int = 0, size: Int = 20): Result<PagedWalletTransactionResponse> =
        walletApiService.fetchWalletTransactions(walletId, page, size)

    suspend fun previewExchange(
        sourceCurrency: String,
        targetCurrency: String,
        amount: Double
    ): Result<CurrencyExchangeResponse> =
        walletApiService.previewExchange(sourceCurrency, targetCurrency, amount)
}
