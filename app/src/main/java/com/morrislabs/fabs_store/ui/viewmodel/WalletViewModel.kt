package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.model.WalletDTO
import com.morrislabs.fabs_store.data.model.WalletTransactionDTO
import com.morrislabs.fabs_store.data.model.WithdrawRequest
import com.morrislabs.fabs_store.data.repository.WalletRepository
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "WalletViewModel"
    }

    private val context = application.applicationContext
    private val tokenManager = TokenManager.getInstance(context)
    private val walletRepository = WalletRepository(context, tokenManager)

    private val _walletState = MutableStateFlow<WalletLoadingState<WalletDTO>>(WalletLoadingState.Idle)
    val walletState: StateFlow<WalletLoadingState<WalletDTO>> = _walletState.asStateFlow()

    private val _transactionsState = MutableStateFlow<WalletLoadingState<List<WalletTransactionDTO>>>(WalletLoadingState.Idle)
    val transactionsState: StateFlow<WalletLoadingState<List<WalletTransactionDTO>>> = _transactionsState.asStateFlow()

    private val _withdrawState = MutableStateFlow<WithdrawState>(WithdrawState.Idle)
    val withdrawState: StateFlow<WithdrawState> = _withdrawState.asStateFlow()

    fun fetchWallet(storeId: String) {
        _walletState.value = WalletLoadingState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Fetching wallet for store: $storeId")

            walletRepository.fetchStoreWallet(storeId)
                .onSuccess { wallet ->
                    Log.d(TAG, "Wallet fetched successfully: balance=${wallet.balance}")
                    _walletState.value = WalletLoadingState.Success(wallet)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to fetch wallet"
                    Log.e(TAG, "Fetch wallet failed: $errorMessage", error)
                    _walletState.value = WalletLoadingState.Error(errorMessage)
                }
        }
    }

    fun fetchTransactions(storeId: String, page: Int = 0) {
        _transactionsState.value = WalletLoadingState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Fetching transactions for store: $storeId (page: $page)")

            walletRepository.fetchStoreTransactions(storeId, page)
                .onSuccess { pagedResponse ->
                    Log.d(TAG, "Transactions fetched: ${pagedResponse.content.size} items")
                    _transactionsState.value = WalletLoadingState.Success(pagedResponse.content)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to fetch transactions"
                    Log.e(TAG, "Fetch transactions failed: $errorMessage", error)
                    _transactionsState.value = WalletLoadingState.Error(errorMessage)
                }
        }
    }

    fun initiateWithdrawal(storeId: String, phoneNumber: String, amount: Double) {
        _withdrawState.value = WithdrawState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Initiating withdrawal for store: $storeId, amount: $amount")

            val request = WithdrawRequest(phoneNumber = phoneNumber, amount = amount)
            walletRepository.initiateWithdrawal(storeId, request)
                .onSuccess { wallet ->
                    Log.d(TAG, "Withdrawal successful, new balance: ${wallet.balance}")
                    _withdrawState.value = WithdrawState.Success(wallet)
                    _walletState.value = WalletLoadingState.Success(wallet)
                    fetchTransactions(storeId)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to initiate withdrawal"
                    Log.e(TAG, "Withdrawal failed: $errorMessage", error)
                    _withdrawState.value = WithdrawState.Error(errorMessage)
                }
        }
    }

    fun resetWithdrawState() {
        _withdrawState.value = WithdrawState.Idle
    }

    sealed class WalletLoadingState<out T> {
        data object Idle : WalletLoadingState<Nothing>()
        data object Loading : WalletLoadingState<Nothing>()
        data class Success<T>(val data: T) : WalletLoadingState<T>()
        data class Error(val message: String) : WalletLoadingState<Nothing>()
    }

    sealed class WithdrawState {
        data object Idle : WithdrawState()
        data object Loading : WithdrawState()
        data class Success(val wallet: WalletDTO) : WithdrawState()
        data class Error(val message: String) : WithdrawState()
    }
}
