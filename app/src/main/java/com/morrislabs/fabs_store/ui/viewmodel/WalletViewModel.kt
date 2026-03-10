package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.model.WalletDTO
import com.morrislabs.fabs_store.data.model.WalletTransactionDTO
import com.morrislabs.fabs_store.data.model.WithdrawRequest
import com.morrislabs.fabs_store.data.model.PayoutRequestPayload
import com.morrislabs.fabs_store.data.model.PayoutResponseDTO
import com.morrislabs.fabs_store.data.model.CurrencyExchangeRequest
import com.morrislabs.fabs_store.data.model.CurrencyExchangeResponse
import com.morrislabs.fabs_store.data.repository.WalletRepository
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

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

    private val _payoutState = MutableStateFlow<PayoutState>(PayoutState.Idle)
    val payoutState: StateFlow<PayoutState> = _payoutState.asStateFlow()

    private val _payoutsListState = MutableStateFlow<WalletLoadingState<List<PayoutResponseDTO>>>(WalletLoadingState.Idle)
    val payoutsListState: StateFlow<WalletLoadingState<List<PayoutResponseDTO>>> = _payoutsListState.asStateFlow()

    private val _exchangeState = MutableStateFlow<ExchangeState>(ExchangeState.Idle)
    val exchangeState: StateFlow<ExchangeState> = _exchangeState.asStateFlow()

    private val _allWalletsState = MutableStateFlow<WalletLoadingState<List<WalletDTO>>>(WalletLoadingState.Idle)
    val allWalletsState: StateFlow<WalletLoadingState<List<WalletDTO>>> = _allWalletsState.asStateFlow()

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
                    _transactionsState.value = WalletLoadingState.Success(
                        pagedResponse.content.sortedByDescending { transaction ->
                            transaction.dateCreated
                                ?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
                                ?: Long.MIN_VALUE
                        }
                    )
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to fetch transactions"
                    Log.e(TAG, "Fetch transactions failed: $errorMessage", error)
                    _transactionsState.value = WalletLoadingState.Error(errorMessage)
                }
        }
    }

    fun initiateWithdrawal(
        storeId: String,
        amount: Double,
        disbursementMethod: String,
        phoneNumber: String? = null,
        stripeConnectedAccountId: String? = null
    ) {
        _withdrawState.value = WithdrawState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Initiating withdrawal for store: $storeId, amount: $amount, method: $disbursementMethod")

            val request = WithdrawRequest(
                phoneNumber = phoneNumber,
                amount = amount,
                disbursementMethod = disbursementMethod,
                stripeConnectedAccountId = stripeConnectedAccountId
            )
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

    fun fetchAllWallets(storeId: String) {
        _allWalletsState.value = WalletLoadingState.Loading
        viewModelScope.launch {
            walletRepository.fetchAllStoreWallets(storeId)
                .onSuccess { wallets ->
                    Log.d(TAG, "All wallets fetched: ${wallets.size} currencies")
                    _allWalletsState.value = WalletLoadingState.Success(wallets)
                }
                .onFailure { error ->
                    Log.e(TAG, "Fetch all wallets failed: ${error.message}", error)
                    _allWalletsState.value = WalletLoadingState.Error(error.message ?: "Failed to fetch wallets")
                }
        }
    }

    fun requestPayout(
        storeId: String,
        amount: Double,
        currencyCode: String,
        disbursementMethod: String,
        payoutDestination: String? = null,
        stripeConnectedAccountId: String? = null
    ) {
        _payoutState.value = PayoutState.Loading
        viewModelScope.launch {
            Log.d(TAG, "Requesting payout: storeId=$storeId, amount=$amount $currencyCode, method=$disbursementMethod")
            val request = PayoutRequestPayload(
                amount = amount,
                currencyCode = currencyCode,
                payoutDestination = payoutDestination,
                disbursementMethod = disbursementMethod,
                stripeConnectedAccountId = stripeConnectedAccountId
            )
            walletRepository.requestPayout(storeId, request)
                .onSuccess { payout ->
                    Log.d(TAG, "Payout requested successfully: ${payout.id}")
                    _payoutState.value = PayoutState.Success(payout)
                    fetchPayouts(storeId)
                }
                .onFailure { error ->
                    Log.e(TAG, "Payout request failed: ${error.message}", error)
                    _payoutState.value = PayoutState.Error(error.message ?: "Failed to request payout")
                }
        }
    }

    fun fetchPayouts(storeId: String, page: Int = 0) {
        _payoutsListState.value = WalletLoadingState.Loading
        viewModelScope.launch {
            walletRepository.fetchPayouts(storeId, page)
                .onSuccess { response ->
                    Log.d(TAG, "Payouts fetched: ${response.content.size} items")
                    _payoutsListState.value = WalletLoadingState.Success(response.content)
                }
                .onFailure { error ->
                    Log.e(TAG, "Fetch payouts failed: ${error.message}", error)
                    _payoutsListState.value = WalletLoadingState.Error(error.message ?: "Failed to fetch payouts")
                }
        }
    }

    fun exchangeCurrency(
        storeId: String,
        sourceCurrencyCode: String,
        targetCurrencyCode: String,
        amount: Double
    ) {
        _exchangeState.value = ExchangeState.Loading
        viewModelScope.launch {
            Log.d(TAG, "Exchanging: $amount $sourceCurrencyCode -> $targetCurrencyCode")
            val request = CurrencyExchangeRequest(
                sourceCurrencyCode = sourceCurrencyCode,
                targetCurrencyCode = targetCurrencyCode,
                amount = amount
            )
            walletRepository.exchangeCurrency(storeId, request)
                .onSuccess { response ->
                    Log.d(TAG, "Exchange successful: ${response.targetAmount} $targetCurrencyCode")
                    _exchangeState.value = ExchangeState.Success(response)
                    fetchAllWallets(storeId)
                    fetchWallet(storeId)
                }
                .onFailure { error ->
                    Log.e(TAG, "Exchange failed: ${error.message}", error)
                    _exchangeState.value = ExchangeState.Error(error.message ?: "Exchange failed")
                }
        }
    }

    fun resetPayoutState() {
        _payoutState.value = PayoutState.Idle
    }

    fun resetExchangeState() {
        _exchangeState.value = ExchangeState.Idle
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

    sealed class PayoutState {
        data object Idle : PayoutState()
        data object Loading : PayoutState()
        data class Success(val payout: PayoutResponseDTO) : PayoutState()
        data class Error(val message: String) : PayoutState()
    }

    sealed class ExchangeState {
        data object Idle : ExchangeState()
        data object Loading : ExchangeState()
        data class Success(val result: CurrencyExchangeResponse) : ExchangeState()
        data class Error(val message: String) : ExchangeState()
    }
}
