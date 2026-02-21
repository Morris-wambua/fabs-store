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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.ReservationFilter
import com.morrislabs.fabs_store.data.model.ReservationTransitionAction
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ReservationsTabContent(
    storeId: String,
    storeViewModel: StoreViewModel,
    reservationsState: StoreViewModel.LoadingState<List<ReservationWithPaymentDTO>>,
    isRefreshing: Boolean,
    selectedFilter: ReservationFilter,
    onFilterChange: (ReservationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedReservation by remember { mutableStateOf<ReservationWithPaymentDTO?>(null) }
    var showWalkInBooking by remember { mutableStateOf(false) }
    val currentFilterStatus = selectedFilter.toBackendStatus()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            if (storeId.isBlank()) {
                return@rememberPullRefreshState
            }
            val query = searchQuery.trim().ifBlank { null }
            storeViewModel.refreshReservations(storeId, currentFilterStatus, query)
        }
    )

    LaunchedEffect(storeId, selectedFilter, searchQuery) {
        if (storeId.isBlank()) {
            return@LaunchedEffect
        }
        delay(300)
        val query = searchQuery.trim().ifBlank { null }
        storeViewModel.fetchReservations(storeId, currentFilterStatus, query)
    }

    if (selectedReservation != null) {
        ReservationDetailsScreen(
            reservation = selectedReservation!!,
            selectedFilter = selectedFilter,
            onNavigateBack = { selectedReservation = null }
        )
        return
    }

    if (showWalkInBooking) {
        WalkInBookingScreen(
            storeId = storeId,
            storeViewModel = storeViewModel,
            onNavigateBack = { showWalkInBooking = false },
            onBookingCreated = { showWalkInBooking = false }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReservationsHeader()

            ReservationSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ReservationFilterRow(
                selectedFilter = selectedFilter,
                onFilterChange = onFilterChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (reservationsState) {
                    is StoreViewModel.LoadingState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is StoreViewModel.LoadingState.Success -> {
                        ReservationsListContent(
                            reservations = reservationsState.data,
                            selectedFilter = selectedFilter,
                            onDetailsClick = { selectedReservation = it },
                            onTransition = { reservationId, action ->
                                storeViewModel.transitionReservation(
                                    reservationId = reservationId,
                                    action = action,
                                    storeId = storeId,
                                    filterStatus = currentFilterStatus,
                                    query = searchQuery.trim().ifBlank { null }
                                )
                            }
                        )
                    }
                    is StoreViewModel.LoadingState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Error: ${reservationsState.message}")
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No reservations yet")
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

        FloatingActionButton(
            onClick = { showWalkInBooking = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 30.dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary

        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create walk-in booking",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

private fun ReservationFilter.toBackendStatus(): String = when (this) {
    ReservationFilter.PENDING_APPROVAL -> "PENDING_APPROVAL"
    ReservationFilter.UPCOMING -> "BOOKED_ACCEPTED"
    ReservationFilter.IN_PROGRESS -> "ACTIVE_SERVICE"
    ReservationFilter.CANCELLED -> "CANCELLED"
    ReservationFilter.COMPLETED -> "SERVED"
    ReservationFilter.LAPSED_PAID -> "LAPSED_PAID"
    ReservationFilter.LAPSED_NOT_ACCEPTED -> "LAPSED_NOT_ACCEPTED"
}

@Composable
private fun ReservationsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
        Text(
            text = "Reservations",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Box {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 7.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
private fun ReservationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search by customer or service...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        singleLine = true
    )
}

@Composable
private fun ReservationFilterRow(
    selectedFilter: ReservationFilter,
    onFilterChange: (ReservationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ReservationFilter.entries.size) { index ->
            val filter = ReservationFilter.entries[index]
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        filter.displayName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun ReservationsListContent(
    reservations: List<ReservationWithPaymentDTO>,
    selectedFilter: ReservationFilter,
    onDetailsClick: (ReservationWithPaymentDTO) -> Unit,
    onTransition: (String, ReservationTransitionAction) -> Unit
) {
    if (reservations.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No reservations found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            reservations.forEach { reservation ->
                ReservationRow(
                    reservation = reservation,
                    selectedFilter = selectedFilter,
                    onDetailsClick = { onDetailsClick(reservation) },
                    onTransitionClick = onTransition
                )
            }
        }
    }
}
