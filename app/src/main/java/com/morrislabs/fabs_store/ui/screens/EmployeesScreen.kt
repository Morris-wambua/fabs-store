package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.ui.components.expert.ExpertCard
import com.morrislabs.fabs_store.ui.components.expert.ExpertListItem
import com.morrislabs.fabs_store.ui.viewmodel.ExpertViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    onNavigateBack: () -> Unit,
    onExpertSelected: (String) -> Unit = {},
    storeViewModel: StoreViewModel = viewModel(),
    expertViewModel: ExpertViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val expertsState by expertViewModel.expertsState.collectAsState()

    val storeId = when (storeState) {
        is StoreViewModel.StoreState.Success -> (storeState as StoreViewModel.StoreState.Success).data.id ?: ""
        else -> ""
    }

    LaunchedEffect(storeId) {
        if (storeId.isNotEmpty()) {
            expertViewModel.getExpertsByStoreId(storeId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Our Experts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (expertsState) {
            is ExpertViewModel.ExpertsState.Loading -> {
                LoadingState()
            }
            is ExpertViewModel.ExpertsState.Error -> {
                val errorMessage = (expertsState as ExpertViewModel.ExpertsState.Error).message
                ErrorState(errorMessage) {
                    if (storeId.isNotEmpty()) {
                        expertViewModel.getExpertsByStoreId(storeId)
                    }
                }
            }
            is ExpertViewModel.ExpertsState.Success -> {
                val experts = (expertsState as ExpertViewModel.ExpertsState.Success).experts

                if (experts.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Featured Experts",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            FeaturedExpertsRow(
                                experts = experts.take(3),
                                storeId = storeId,
                                onExpertClick = { onExpertSelected(it.id) }
                            )
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                text = "All Experts",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(experts) { expert ->
                            ExpertListItem(
                                expert = expert,
                                storeId = storeId,
                                onExpertClick = { onExpertSelected(expert.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
            else -> {
                LoadingState()
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(errorMessage: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Error: $errorMessage")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No experts found for this store.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun FeaturedExpertsRow(
    experts: List<ExpertDTO>,
    storeId: String,
    onExpertClick: (ExpertDTO) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(end = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        items(experts) { expert ->
            ExpertCard(
                expert = expert,
                storeId = storeId,
                onExpertClick = { onExpertClick(expert) },
                modifier = Modifier.width(140.dp)
            )
        }
    }
}
