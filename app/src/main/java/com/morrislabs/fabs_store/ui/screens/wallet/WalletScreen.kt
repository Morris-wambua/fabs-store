package com.morrislabs.fabs_store.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    val allWalletsState by walletViewModel.allWalletsState.collectAsState()
    val walletTransactionsMap by walletViewModel.walletTransactionsMap.collectAsState()
    val withdrawState by walletViewModel.withdrawState.collectAsState()
    val exchangeState by walletViewModel.exchangeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedWalletForWithdraw by remember { mutableStateOf<WalletDTO?>(null) }
    var showExchangeSheet by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(storeId) {
        if (storeId.isNotEmpty()) {
            walletViewModel.fetchAllWallets(storeId)
        }
    }

    val wallets = when (val state = allWalletsState) {
        is WalletViewModel.WalletLoadingState.Success -> state.data
        else -> emptyList()
    }

    LaunchedEffect(wallets) {
        wallets.forEach { wallet ->
            wallet.id?.let { walletViewModel.fetchTransactionsForWallet(it) }
        }
        if (selectedTabIndex >= wallets.size && wallets.isNotEmpty()) {
            selectedTabIndex = 0
        }
        isRefreshing = false
    }

    LaunchedEffect(withdrawState) {
        when (withdrawState) {
            is WalletViewModel.WithdrawState.Success -> {
                selectedWalletForWithdraw = null
                snackbarHostState.showSnackbar("Withdrawal initiated successfully")
                walletViewModel.resetWithdrawState()
                walletViewModel.fetchAllWallets(storeId)
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

    LaunchedEffect(exchangeState) {
        when (exchangeState) {
            is WalletViewModel.ExchangeState.Success -> {
                showExchangeSheet = false
                snackbarHostState.showSnackbar("Currency converted successfully")
                walletViewModel.resetExchangeState()
                walletViewModel.fetchAllWallets(storeId)
            }
            is WalletViewModel.ExchangeState.Error -> {
                snackbarHostState.showSnackbar(
                    (exchangeState as WalletViewModel.ExchangeState.Error).message
                )
                walletViewModel.resetExchangeState()
            }
            else -> {}
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            if (storeId.isNotEmpty()) {
                isRefreshing = true
                walletViewModel.fetchAllWallets(storeId)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallet",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
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
            when (allWalletsState) {
                is WalletViewModel.WalletLoadingState.Idle,
                is WalletViewModel.WalletLoadingState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is WalletViewModel.WalletLoadingState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (allWalletsState as WalletViewModel.WalletLoadingState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { walletViewModel.fetchAllWallets(storeId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is WalletViewModel.WalletLoadingState.Success -> {
                    if (wallets.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No wallets found", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (wallets.size > 1) {
                                ScrollableTabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    edgePadding = 16.dp,
                                    divider = {}
                                ) {
                                    wallets.forEachIndexed { index, wallet ->
                                        Tab(
                                            selected = selectedTabIndex == index,
                                            onClick = { selectedTabIndex = index },
                                            text = {
                                                Text(
                                                    text = wallet.currency,
                                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            val selectedWallet = wallets.getOrNull(selectedTabIndex) ?: wallets.first()
                            val txState = walletTransactionsMap[selectedWallet.id]
                            val transactions = when (txState) {
                                is WalletViewModel.WalletLoadingState.Success -> txState.data
                                else -> emptyList()
                            }
                            val txLoading = txState is WalletViewModel.WalletLoadingState.Loading

                            WalletTabContent(
                                wallet = selectedWallet,
                                transactions = transactions,
                                transactionsLoading = txLoading,
                                showTransferButton = wallets.size > 1,
                                onWithdrawClick = { selectedWalletForWithdraw = selectedWallet },
                                onTransferClick = { showExchangeSheet = true }
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    selectedWalletForWithdraw?.let { selectedWallet ->
        WithdrawBottomSheet(
            balance = selectedWallet.balance,
            currency = selectedWallet.currency,
            isLoading = withdrawState is WalletViewModel.WithdrawState.Loading,
            onDismiss = { selectedWalletForWithdraw = null },
            onWithdraw = { amount, method, phoneNumber, stripeConnectedAccountId ->
                walletViewModel.initiateWithdrawal(
                    storeId = storeId,
                    amount = amount,
                    disbursementMethod = method,
                    phoneNumber = phoneNumber,
                    stripeConnectedAccountId = stripeConnectedAccountId,
                    currencyCode = selectedWallet.currency
                )
            }
        )
    }

    if (showExchangeSheet && wallets.size > 1) {
        val selectedWallet = wallets.getOrNull(selectedTabIndex) ?: wallets.first()
        CurrencyExchangeBottomSheet(
            wallets = wallets,
            sourceWallet = selectedWallet,
            exchangeState = exchangeState,
            previewState = walletViewModel.previewState.collectAsState().value,
            onPreview = { source, target, amount ->
                walletViewModel.previewExchange(source, target, amount)
            },
            onExchange = { source, target, amount ->
                walletViewModel.exchangeCurrency(storeId, source, target, amount)
            },
            onDismiss = {
                showExchangeSheet = false
                walletViewModel.resetPreviewState()
            }
        )
    }
}

@Composable
private fun WalletTabContent(
    wallet: WalletDTO,
    transactions: List<WalletTransactionDTO>,
    transactionsLoading: Boolean,
    showTransferButton: Boolean,
    onWithdrawClick: () -> Unit,
    onTransferClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            WalletBalanceCard(balance = wallet.balance, currency = wallet.currency)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onWithdrawClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Withdraw", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
                if (showTransferButton) {
                    OutlinedButton(
                        onClick = onTransferClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Convert", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Transaction History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        if (transactionsLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        } else if (transactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No transactions yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(transactions, key = { it.id ?: it.hashCode().toString() }) { transaction ->
                TransactionItem(transaction = transaction, walletCurrencyCode = wallet.currency)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
