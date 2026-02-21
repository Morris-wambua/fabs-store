package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.ReservationFilter
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@Composable
fun ReservationsScreen(
    onNavigateBack: () -> Unit,
    storeViewModel: StoreViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val reservationsState by storeViewModel.reservationsState.collectAsState()
    val isRefreshing by storeViewModel.isRefreshing.collectAsState()
    var selectedFilter by remember { mutableStateOf(ReservationFilter.PENDING_APPROVAL) }

    LaunchedEffect(Unit) {
        storeViewModel.fetchUserStore()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = storeState) {
            is StoreViewModel.StoreState.Success -> {
                ReservationsTabContent(
                    storeId = state.data.id.orEmpty(),
                    storeViewModel = storeViewModel,
                    reservationsState = reservationsState,
                    isRefreshing = isRefreshing,
                    selectedFilter = selectedFilter,
                    onFilterChange = { newFilter ->
                        selectedFilter = newFilter
                    }
                )
            }
            is StoreViewModel.StoreState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading reservations...")
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Unable to load reservations")
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.0f)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
