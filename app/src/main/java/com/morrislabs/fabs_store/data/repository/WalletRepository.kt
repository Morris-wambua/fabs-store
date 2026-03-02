package com.morrislabs.fabs_store.data.repository

import android.content.Context
import com.morrislabs.fabs_store.data.api.WalletApiService
import com.morrislabs.fabs_store.data.model.PagedWalletTransactionResponse
import com.morrislabs.fabs_store.data.model.WalletDTO
import com.morrislabs.fabs_store.data.model.WithdrawRequest
import com.morrislabs.fabs_store.util.TokenManager

class WalletRepository(context: Context, tokenManager: TokenManager) {
    private val walletApiService = WalletApiService(context, tokenManager)

    suspend fun fetchStoreWallet(storeId: String): Result<WalletDTO> =
        walletApiService.fetchStoreWallet(storeId)

    suspend fun fetchStoreTransactions(
        storeId: String,
        page: Int = 0,
        size: Int = 20
    ): Result<PagedWalletTransactionResponse> =
        walletApiService.fetchStoreTransactions(storeId, page, size)

    suspend fun initiateWithdrawal(storeId: String, request: WithdrawRequest): Result<WalletDTO> =
        walletApiService.initiateWithdrawal(storeId, request)
}
