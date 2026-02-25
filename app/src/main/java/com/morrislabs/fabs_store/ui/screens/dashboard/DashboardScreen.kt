package com.morrislabs.fabs_store.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.morrislabs.fabs_store.data.model.FetchStoreResponse
import com.morrislabs.fabs_store.data.model.ReservationStatus
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO
import com.morrislabs.fabs_store.ui.viewmodel.ReviewViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import java.time.LocalTime
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    store: FetchStoreResponse,
    reservationsState: StoreViewModel.LoadingState<List<ReservationWithPaymentDTO>>,
    reviewsState: ReviewViewModel.ReviewsState,
    checklistSteps: List<ChecklistStep>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateExpert: (String) -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToDailySchedule: () -> Unit,
    onNavigateToReservations: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val storeId = store.id ?: ""
    val greeting = getGreeting()
    var bannerVisible by remember { mutableStateOf(true) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            DashboardHeader(
                greeting = greeting,
                storeName = store.name,
                logoUrl = store.logoUrl,
                onSettings = onNavigateToSettings,
                onNotifications = {}
            )

            SetupChecklistBanner(
                steps = checklistSteps,
                visible = bannerVisible,
                onDismiss = { bannerVisible = false },
                onNavigateToChecklist = onNavigateToChecklist
            )

        Spacer(modifier = Modifier.height(20.dp))

        TodaysInsightsSection(reservationsState)

        Spacer(modifier = Modifier.height(24.dp))

        QuickActionsSection(
            storeId = storeId,
            onAddExpert = onNavigateToCreateExpert,
            onNewService = onNavigateToServices,
            onSchedule = onNavigateToReservations,
            onDailySchedule = onNavigateToDailySchedule,
            onReviews = onNavigateToReviews
        )

        Spacer(modifier = Modifier.height(24.dp))

        UpcomingSection(
            reservationsState = reservationsState,
            onViewAll = onNavigateToReservations
        )

        Spacer(modifier = Modifier.height(24.dp))

        RecentFeedbackSection(
            reviewsState = reviewsState,
            onViewAll = onNavigateToReviews
        )

        Spacer(modifier = Modifier.height(100.dp))
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun DashboardHeader(
    greeting: String,
    storeName: String,
    logoUrl: String?,
    onSettings: () -> Unit,
    onNotifications: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ) {
            if (!logoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Store Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting,",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = storeName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        IconButton(onClick = onSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onNotifications) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodaysInsightsSection(
    reservationsState: StoreViewModel.LoadingState<List<ReservationWithPaymentDTO>>
) {
    val reservations = when (reservationsState) {
        is StoreViewModel.LoadingState.Success -> reservationsState.data
        else -> emptyList()
    }

    val activeCount = reservations.count {
        it.status == ReservationStatus.BOOKED_ACCEPTED ||
                it.status == ReservationStatus.IN_PROGRESS ||
                it.status == ReservationStatus.PENDING_FINAL_PAYMENT
    }
    val earnings = reservations
        .filter { it.status == ReservationStatus.SERVED }
        .sumOf { it.price }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Insights",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Live",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            item {
                InsightCard(
                    title = "ACTIVE RESERVATIONS",
                    value = "$activeCount",
                    delta = "today",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                InsightCard(
                    title = "PENDING APPROVAL",
                    value = "${reservations.count { it.status == ReservationStatus.BOOKED_PENDING_ACCEPTANCE || it.status == ReservationStatus.BOOKED_PENDING_PAYMENT }}",
                    delta = "awaiting action",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
            item {
                InsightCard(
                    title = "EARNINGS TODAY",
                    value = "KES ${String.format("%.0f", earnings)}",
                    delta = "from completed",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    storeId: String,
    onAddExpert: (String) -> Unit,
    onNewService: () -> Unit,
    onSchedule: () -> Unit,
    onDailySchedule: () -> Unit,
    onReviews: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader(title = "QUICK ACTIONS")
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.PersonAdd,
                label = "Add Expert",
                onClick = { onAddExpert(storeId) }
            )
            QuickActionButton(
                icon = Icons.Default.AutoAwesome,
                label = "New Service",
                onClick = onNewService
            )
            QuickActionButton(
                icon = Icons.Default.CalendarMonth,
                label = "Schedule",
                onClick = onSchedule
            )
            QuickActionButton(
                icon = Icons.Default.Schedule,
                label = "Daily Schedule",
                onClick = onDailySchedule
            )
            QuickActionButton(
                icon = Icons.Default.Star,
                label = "Reviews",
                onClick = onReviews
            )
        }
    }
}

@Composable
private fun UpcomingSection(
    reservationsState: StoreViewModel.LoadingState<List<ReservationWithPaymentDTO>>,
    onViewAll: () -> Unit
) {
    val upcoming = when (reservationsState) {
        is StoreViewModel.LoadingState.Success -> reservationsState.data
            .filter {
                it.status == ReservationStatus.BOOKED_ACCEPTED ||
                        it.status == ReservationStatus.BOOKED_PENDING_ACCEPTANCE ||
                        it.status == ReservationStatus.BOOKED_PENDING_PAYMENT
            }
            .take(3)
        else -> emptyList()
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader(title = "Upcoming") {
            TextButton(onClick = onViewAll) {
                Text("View All", style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (upcoming.isEmpty()) {
            Text(
                text = "No upcoming appointments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            upcoming.forEach { reservation ->
                val statusLabel = when (reservation.status) {
                    ReservationStatus.BOOKED_PENDING_PAYMENT -> "Awaiting Payment"
                    ReservationStatus.BOOKED_ACCEPTED -> "Confirmed"
                    ReservationStatus.BOOKED_PENDING_ACCEPTANCE -> "Pending"
                    else -> reservation.status.name
                }
                UpcomingAppointmentCard(
                    customerName = reservation.name,
                    serviceName = reservation.typeOfServiceName.ifEmpty { "Service" },
                    time = "${reservation.startTime} - ${reservation.endTime}",
                    status = statusLabel
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RecentFeedbackSection(
    reviewsState: ReviewViewModel.ReviewsState,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        val reviews = when (reviewsState) {
            is ReviewViewModel.ReviewsState.Success -> reviewsState.reviews
            else -> emptyList()
        }

        SectionHeader(
            title = "RECENT FEEDBACK",
            trailing = if (reviews.size > 6) {
                {
                    TextButton(onClick = onViewAll) {
                        Text("View All", style = MaterialTheme.typography.labelMedium)
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else null
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (reviewsState) {
            is ReviewViewModel.ReviewsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            is ReviewViewModel.ReviewsState.Success -> {
                if (reviews.isEmpty()) {
                    EmptyFeedbackCard()
                } else {
                    val displayReviews = reviews.take(6)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(displayReviews) { review ->
                            ReviewCard(
                                customerName = review.displayName
                                    ?: review.userName,
                                rating = review.rating.toInt(),
                                review = review.comment
                            )
                        }
                    }
                }
            }
            else -> {
                EmptyFeedbackCard()
            }
        }
    }
}

@Composable
private fun EmptyFeedbackCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No store reviews yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getGreeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}
