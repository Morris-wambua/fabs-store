package com.morrislabs.fabs_store.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.WalletDTO
import com.morrislabs.fabs_store.data.model.WalletTransactionDTO
import com.morrislabs.fabs_store.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun WalletScreen(
    storeId: String,
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    val walletState by walletViewModel.walletState.collectAsState()
    val transactionsState by walletViewModel.transactionsState.collectAsState()
    val withdrawState by walletViewModel.withdrawState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showWithdrawSheet by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(storeId) {
        if (storeId.isNotEmpty()) {
            walletViewModel.fetchWallet(storeId)
            walletViewModel.fetchTransactions(storeId)
        }
    }

    LaunchedEffect(withdrawState) {
        when (withdrawState) {
            is WalletViewModel.WithdrawState.Success -> {
                showWithdrawSheet = false
                snackbarHostState.showSnackbar("Withdrawal initiated successfully")
                walletViewModel.resetWithdrawState()
            }
            is WalletViewModel.WithdrawState.Error -> {
                snackbarHostState.showSnackbar(
                    (withdrawState as WalletViewModel.WithdrawState.Error).message
                )
                walletViewModel.resetWithdrawState()
            }
            else -> {}
        }
    }

    LaunchedEffect(walletState) {
        if (walletState !is WalletViewModel.WalletLoadingState.Loading) {
            isRefreshing = false
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            if (storeId.isNotEmpty()) {
                isRefreshing = true
                walletViewModel.fetchWallet(storeId)
                walletViewModel.fetchTransactions(storeId)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallet",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when (walletState) {
                is WalletViewModel.WalletLoadingState.Idle,
                is WalletViewModel.WalletLoadingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is WalletViewModel.WalletLoadingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (walletState as WalletViewModel.WalletLoadingState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                walletViewModel.fetchWallet(storeId)
                                walletViewModel.fetchTransactions(storeId)
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is WalletViewModel.WalletLoadingState.Success -> {
                    val wallet = (walletState as WalletViewModel.WalletLoadingState.Success<WalletDTO>).data
                    val transactions = when (transactionsState) {
                        is WalletViewModel.WalletLoadingState.Success ->
                            (transactionsState as WalletViewModel.WalletLoadingState.Success<List<WalletTransactionDTO>>).data
                        else -> emptyList()
                    }

                    WalletContent(
                        wallet = wallet,
                        transactions = transactions,
                        transactionsLoading = transactionsState is WalletViewModel.WalletLoadingState.Loading,
                        onWithdrawClick = { showWithdrawSheet = true }
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    if (showWithdrawSheet) {
        val wallet = (walletState as? WalletViewModel.WalletLoadingState.Success<WalletDTO>)?.data
        if (wallet != null) {
            WithdrawBottomSheet(
                balance = wallet.balance,
                currency = wallet.currency,
                isLoading = withdrawState is WalletViewModel.WithdrawState.Loading,
                onDismiss = { showWithdrawSheet = false },
                onWithdraw = { phoneNumber, amount ->
                    walletViewModel.initiateWithdrawal(storeId, phoneNumber, amount)
                }
            )
        }
    }
}

@Composable
private fun WalletContent(
    wallet: WalletDTO,
    transactions: List<WalletTransactionDTO>,
    transactionsLoading: Boolean,
    onWithdrawClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            WalletBalanceCard(
                balance = wallet.balance,
                currency = wallet.currency
            )
        }

        item {
            Button(
                onClick = onWithdrawClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Withdraw",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (transactionsLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        } else if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(transactions, key = { it.id ?: it.hashCode().toString() }) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
