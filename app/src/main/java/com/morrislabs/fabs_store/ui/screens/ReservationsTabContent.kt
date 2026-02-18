package com.morrislabs.fabs_store.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.ReservationFilter
import com.morrislabs.fabs_store.data.model.ReservationStatus
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

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
    Column(modifier = modifier.fillMaxSize()) {
        ReservationFilterRow(
            selectedFilter = selectedFilter,
            onFilterChange = onFilterChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = {
                val filterStatus = when (selectedFilter) {
                    ReservationFilter.PENDING_APPROVAL -> "BOOKED_PENDING_ACCEPTANCE"
                    ReservationFilter.UPCOMING -> "BOOKED_ACCEPTED"
                    ReservationFilter.CANCELLED -> "CANCELLED"
                    ReservationFilter.COMPLETED -> "SERVED"
                    ReservationFilter.LAPSED_PAID -> "LAPSED_PAID"
                    ReservationFilter.LAPSED_NOT_ACCEPTED -> "LAPSED_NOT_ACCEPTED"
                }
                storeViewModel.refreshReservations(storeId, filterStatus)
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pullRefresh(pullRefreshState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                when (reservationsState) {
                    is StoreViewModel.LoadingState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is StoreViewModel.LoadingState.Success -> {
                        ReservationsListContent(reservationsState.data)
                    }
                    is StoreViewModel.LoadingState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: ${reservationsState.message}", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No reservations yet", style = MaterialTheme.typography.bodyLarge)
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
}

@Composable
private fun ReservationsListContent(reservations: List<ReservationWithPaymentDTO>) {
    if (reservations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No reservations found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            reservations.forEach { reservation ->
                ReservationRow(reservation)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
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
                label = { Text(filter.displayName, style = MaterialTheme.typography.labelSmall) },
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun ReservationRow(reservation: ReservationWithPaymentDTO) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reservation.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp).rotate(rotationState)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Event, contentDescription = "Date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = reservation.reservationDate, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                StatusBadge(reservation.status.name)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Price: KES ${reservation.price}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    DetailRow(icon = Icons.Default.Schedule, label = "Time", value = "${reservation.startTime} - ${reservation.endTime}")
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(icon = Icons.Default.People, label = "Expert", value = reservation.reservationExpertName.ifEmpty { "Not assigned" })
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(icon = Icons.Default.Settings, label = "Service", value = reservation.typeOfServiceName.ifEmpty { "Unknown service" })
                    Spacer(modifier = Modifier.height(16.dp))

                    when (reservation.status) {
                        ReservationStatus.BOOKED_PENDING_ACCEPTANCE -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 10.dp)) {
                                    Text("Reject", style = MaterialTheme.typography.labelMedium)
                                }
                                Button(onClick = {}, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 10.dp)) {
                                    Text("Approve", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        ReservationStatus.BOOKED_ACCEPTED -> {
                            Button(onClick = {}, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp)) {
                                Text("Start Session", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val backgroundColor = when {
        status.contains("PENDING") -> MaterialTheme.colorScheme.tertiaryContainer
        status.contains("ACCEPTED") -> MaterialTheme.colorScheme.primaryContainer
        status.contains("REJECTED") || status.contains("CANCELLED") -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        status.contains("PENDING") -> MaterialTheme.colorScheme.onTertiaryContainer
        status.contains("ACCEPTED") -> MaterialTheme.colorScheme.onPrimaryContainer
        status.contains("REJECTED") || status.contains("CANCELLED") -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(modifier = Modifier.padding(4.dp), shape = RoundedCornerShape(4.dp), color = backgroundColor) {
        Text(
            text = status.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}
