package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.ui.viewmodel.ExpertViewModel
import com.morrislabs.fabs_store.ui.viewmodel.PostViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit = {},
    onNavigateToEmployees: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToStoreProfile: () -> Unit = {},
    onNavigateToCreateStore: () -> Unit = {},
    onNavigateToExpertDetails: (String) -> Unit = {},
    onNavigateToCreateExpert: (String) -> Unit = {},
    onNavigateToDailySchedule: () -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToPostDetail: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    storeViewModel: StoreViewModel = viewModel(),
    expertViewModel: ExpertViewModel = viewModel(),
    postViewModel: PostViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()

    LaunchedEffect(Unit) {
        storeViewModel.fetchUserStore()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (storeState) {
            is StoreViewModel.StoreState.Loading -> {
                LoadingScreen()
            }
            is StoreViewModel.StoreState.Success -> {
                val store = (storeState as StoreViewModel.StoreState.Success).data
                MainScreen(
                    store = store,
                    storeViewModel = storeViewModel,
                    expertViewModel = expertViewModel,
                    postViewModel = postViewModel,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToCreateExpert = onNavigateToCreateExpert,
                    onNavigateToExpertDetails = onNavigateToExpertDetails,
                    onNavigateToServices = onNavigateToServices,
                    onNavigateToDailySchedule = onNavigateToDailySchedule,
                    onNavigateToStoreProfile = onNavigateToStoreProfile,
                    onNavigateToCreatePost = onNavigateToCreatePost,
                    onNavigateToPostDetail = onNavigateToPostDetail,
                    onLogout = onLogout
                )
            }
            is StoreViewModel.StoreState.Error.NotFound -> {
                NoStoreFoundOnboardingScreen(
                    onNavigateToCreateStore = onNavigateToCreateStore,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            is StoreViewModel.StoreState.Error.UnknownError -> {
                val error = (storeState as StoreViewModel.StoreState.Error.UnknownError).message
                if (error.contains("No store found", ignoreCase = true) || error.contains("empty", ignoreCase = true)) {
                    NoStoreFoundOnboardingScreen(
                        onNavigateToCreateStore = onNavigateToCreateStore,
                        onNavigateToSettings = onNavigateToSettings
                    )
                } else {
                    ErrorRetryScreen(
                        error = error,
                        onRetry = { storeViewModel.fetchUserStore() },
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            }
            is StoreViewModel.StoreState.Error -> {
                val error = (storeState as StoreViewModel.StoreState.Error).message
                ErrorRetryScreen(
                    error = error,
                    onRetry = { storeViewModel.fetchUserStore() },
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading store information...")
                }
            }
        }
    }
}

@Composable
internal fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun ErrorRetryScreen(
    error: String,
    onRetry: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fabs Store",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Unable to Load Store",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Retry",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
