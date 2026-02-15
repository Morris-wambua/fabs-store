package com.morrislabs.fabs_store.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StoreMallDirectory
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.ReservationFilter
import com.morrislabs.fabs_store.data.model.ReservationStatus
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.ui.components.expert.ExpertCard
import com.morrislabs.fabs_store.ui.components.expert.ExpertListItem
import com.morrislabs.fabs_store.ui.viewmodel.ExpertViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit = {},
    onNavigateToEmployees: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToStoreProfile: () -> Unit = {},
    onNavigateToCreateStore: () -> Unit = {},
    onNavigateToExpertDetails: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    storeViewModel: StoreViewModel = viewModel(),
    expertViewModel: ExpertViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val reservationsState by storeViewModel.reservationsState.collectAsState()
    val isRefreshing by storeViewModel.isRefreshing.collectAsState()
    val expertsState by expertViewModel.expertsState.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedReservationFilter by rememberSaveable { mutableStateOf(ReservationFilter.PENDING_APPROVAL) }
    var storeId by rememberSaveable { mutableStateOf("") }
    val tabs = listOf("Reservations", "Experts", "Services", "Reviews")

    // Create scroll behavior for collapsing toolbar
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                storeId = store.id ?: ""

                LaunchedEffect(storeId) {
                    if (storeId.isNotEmpty()) {
                        expertViewModel.getExpertsByStoreId(storeId)
                    }
                }

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        StoreCollapsingTopAppBar(
                            title = store.name,
                            onSettings = onNavigateToSettings,
                            scrollBehavior = scrollBehavior
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Store Hero Section - Animates based on scroll
                         AnimatedVisibility(
                             visible = scrollBehavior.state.collapsedFraction < 0.9f,
                             enter = fadeIn() + expandVertically(),
                             exit = fadeOut() + shrinkVertically()
                         ) {
                             Column {
                                 StoreCoverHeader(store, onNavigateToStoreProfile)
                                 StoreProfileInfo(store, onNavigateToStoreProfile)
                             }
                         }

                        // Tab Row
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            edgePadding = 16.dp,
                            containerColor = MaterialTheme.colorScheme.background,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                                        .height(3.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            title,
                                            color = if (selectedTabIndex == index)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                        }

                        // Tab Content with sticky filter for reservations
                        if (selectedTabIndex == 0) {
                            // Reservations tab with sticky filter
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                // Sticky filter row - not scrollable
                                ReservationFilterRow(
                                    selectedFilter = selectedReservationFilter,
                                    onFilterChange = { newFilter ->
                                        selectedReservationFilter = newFilter
                                        // Fetch reservations with new filter
                                        val filterStatus = when (newFilter) {
                                            ReservationFilter.PENDING_APPROVAL -> "BOOKED_PENDING_ACCEPTANCE"
                                            ReservationFilter.UPCOMING -> "BOOKED_ACCEPTED"
                                            ReservationFilter.CANCELLED -> "CANCELLED"
                                            ReservationFilter.COMPLETED -> "SERVED"
                                            ReservationFilter.LAPSED_PAID -> "LAPSED_PAID"
                                            ReservationFilter.LAPSED_NOT_ACCEPTED -> "LAPSED_NOT_ACCEPTED"
                                        }
                                        storeViewModel.fetchReservations(storeId, filterStatus)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                )

                                // Scrollable reservations content with pull-to-refresh
                                val pullRefreshState = rememberPullRefreshState(
                                    refreshing = isRefreshing,
                                    onRefresh = {
                                        val filterStatus = when (selectedReservationFilter) {
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
                                                val reservations = (reservationsState as StoreViewModel.LoadingState.Success).data
                                                ReservationsListContent(reservations)
                                            }
                                            is StoreViewModel.LoadingState.Error -> {
                                                val errorMsg = (reservationsState as StoreViewModel.LoadingState.Error).message
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text("Error: $errorMsg", style = MaterialTheme.typography.bodyLarge)
                                                }
                                            }
                                            else -> {
                                                TabContent("No reservations yet", onNavigateToReservations)
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
                        } else {
                            when (selectedTabIndex) {
                                1 -> {
                                    ExpertsTabContent(
                                        expertsState = expertsState,
                                        storeId = storeId,
                                        onExpertClick = { expert -> onNavigateToExpertDetails(expert.id) },
                                        onViewAll = onNavigateToEmployees,
                                        onRetry = {
                                            if (storeId.isNotEmpty()) {
                                                expertViewModel.getExpertsByStoreId(storeId)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    )
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        when (selectedTabIndex) {
                                            2 -> TabContent("Services coming soon", onNavigateToServices)
                                            3 -> TabContent("Reviews coming soon", {})
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.height(24.dp))
                    }
                }
            }
            is StoreViewModel.StoreState.Error.NotFound -> {
                NoStoreScreen(onNavigateToCreateStore = onNavigateToCreateStore, onLogout = onLogout)
            }
            is StoreViewModel.StoreState.Error.UnknownError -> {
                val error = (storeState as StoreViewModel.StoreState.Error.UnknownError).message
                if (error.contains("No store found", ignoreCase = true) || error.contains("empty", ignoreCase = true)) {
                    NoStoreScreen(onNavigateToCreateStore = onNavigateToCreateStore, onLogout = onLogout)
                } else {
                    ErrorRetryScreen(
                        error = error,
                        onRetry = { storeViewModel.fetchUserStore() },
                        onLogout = onLogout
                    )
                }
            }
            is StoreViewModel.StoreState.Error -> {
                val error = (storeState as StoreViewModel.StoreState.Error).message
                ErrorRetryScreen(
                    error = error,
                    onRetry = { storeViewModel.fetchUserStore() },
                    onLogout = onLogout
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreCollapsingTopAppBar(
    title: String,
    onSettings: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    // Determine container color based on scroll state
    val containerColor = lerp(
        Color.Transparent,
        MaterialTheme.colorScheme.surface,
        collapsedFraction
    )

    // Determine content color based on scroll state
    val contentColor = lerp(
        MaterialTheme.colorScheme.inverseSurface, // Dark color when expanded
        MaterialTheme.colorScheme.onSurface,      // Theme color when collapsed
        collapsedFraction
    )

    TopAppBar(
        title = {
            // Show "Store" when expanded, store name when collapsed
            if (collapsedFraction < 0.5f) {
                Text(
                    text = "Store",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                AnimatedVisibility(
                    visible = collapsedFraction > 0.5f,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onSettings,
                modifier = Modifier.semantics { contentDescription = "Settings" }
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun StoreCoverHeader(
    store: com.morrislabs.fabs_store.data.model.FetchStoreResponse,
    onNavigateToStoreProfile: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Default icon (no cover image field in store model)
        Icon(
            imageVector = Icons.Default.StoreMallDirectory,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        // Edit button in top right corner
        IconButton(
            onClick = onNavigateToStoreProfile,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Cover Picture",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StoreProfileInfo(
    store: com.morrislabs.fabs_store.data.model.FetchStoreResponse,
    onNavigateToStoreProfile: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Store name with rating
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = store.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Quick stats row
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${String.format("%.1f", store.ratings)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Location
            store.locationDTO?.name?.let { locationName ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Experts count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${store.noOfExperts}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.People,
                    contentDescription = "Experts",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Experts",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Quick action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { /* Message */ },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Message,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Message")
            }

            OutlinedButton(
                onClick = onNavigateToStoreProfile,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Profile")
            }
        }
    }
}

@Composable
private fun TabContent(
    text: String,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ReservationsListContent(
    reservations: List<ReservationWithPaymentDTO>
) {
    if (reservations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No reservations found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
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
private fun ReservationRow(
    reservation: ReservationWithPaymentDTO
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with name and expand/collapse button
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
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationState)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Basic info row - Date and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = reservation.reservationDate,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status
                StatusBadge(reservation.status.name)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price row - inline format
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Price: KES ${reservation.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Expanded details
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    androidx.compose.material3.Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Time slot
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Time",
                        value = "${reservation.startTime} - ${reservation.endTime}"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expert
                    DetailRow(
                        icon = Icons.Default.People,
                        label = "Expert",
                        value = reservation.reservationExpertName.ifEmpty { "Not assigned" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Service type
                    DetailRow(
                        icon = Icons.Default.Settings,
                        label = "Service",
                        value = reservation.typeOfServiceName.ifEmpty { "Unknown service" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons - different based on status
                    when (reservation.status) {
                        ReservationStatus.BOOKED_PENDING_ACCEPTANCE -> {
                            // Pending approval - show Reject and Approve buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { /* Reject action - implement later */ },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text("Reject", style = MaterialTheme.typography.labelMedium)
                                }
                                Button(
                                    onClick = { /* Approve action - implement later */ },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text("Approve", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        ReservationStatus.BOOKED_ACCEPTED -> {
                            // Upcoming - show Start button for session
                            Button(
                                onClick = { /* Start session - implement later */ },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text("Start Session", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        else -> {
                            // For other statuses (Cancelled, Served, Lapsed), no action buttons
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: String
) {
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

    Surface(
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ErrorRetryScreen(
    error: String,
    onRetry: () -> Unit,
    onLogout: () -> Unit
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
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
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
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
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun NoStoreScreen(
    onNavigateToCreateStore: () -> Unit,
    onLogout: () -> Unit
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
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
            Icon(
                imageVector = Icons.Default.StoreMallDirectory,
                contentDescription = "No Store",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "No Store Found",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Create your store to start managing reservations, employees, and services",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onNavigateToCreateStore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Store",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    "Create Store",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(
    storeName: String,
    onLogout: () -> Unit,
    onSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = storeName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = onSettings,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Reservations", value = "12", modifier = Modifier.weight(1f))
                    StatItem(label = "Employees", value = "5", modifier = Modifier.weight(1f))
                    StatItem(label = "Services", value = "8", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = Color.Gray
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun QuickStatsSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Insights",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InsightRow(
                    label = "Pending Reservations",
                    value = "3",
                    subtext = "Awaiting confirmation"
                )
                InsightRow(
                    label = "Today's Appointments",
                    value = "7",
                    subtext = "Between 9 AM - 6 PM"
                )
                InsightRow(
                    label = "Staff On Leave",
                    value = "1",
                    subtext = "Back tomorrow"
                )
            }
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String,
    subtext: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ManagementCardsSection(
    onNavigateToReservations: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onNavigateToServices: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Manage",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ManagementCard(
            title = "Reservations",
            description = "View and manage customer bookings",
            icon = Icons.Default.Event,
            onClick = onNavigateToReservations,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        ManagementCard(
            title = "Experts",
            description = "Manage team members and schedules",
            icon = Icons.Default.People,
            onClick = onNavigateToEmployees,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        ManagementCard(
            title = "Services",
            description = "Add and edit services you offer",
            icon = Icons.Default.Settings,
            onClick = onNavigateToServices,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ManagementCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Open")
        }
    }
}

@Composable
private fun RecentActivitySection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ActivityItem(
                    title = "New reservation from Sarah Johnson",
                    time = "2 hours ago"
                )
                ActivityItem(
                    title = "John marked as on leave",
                    time = "5 hours ago"
                )
                ActivityItem(
                    title = "Service updated: Hair Styling",
                    time = "1 day ago"
                )
            }
        }
    }
}

@Composable
private fun ActivityItem(
    title: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(top = 2.dp)
                )
                }
                }
                }

                @Composable
                private fun DetailRow(
                icon: androidx.compose.ui.graphics.vector.ImageVector,
                label: String,
                value: String
                ) {
                Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
                ) {
                Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
                )
                }
                }

@Composable
private fun ExpertsTabContent(
    expertsState: ExpertViewModel.ExpertsState,
    storeId: String,
    onExpertClick: (ExpertDTO) -> Unit,
    onViewAll: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (expertsState) {
        is ExpertViewModel.ExpertsState.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ExpertViewModel.ExpertsState.Error -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Could not load experts",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
        is ExpertViewModel.ExpertsState.Success -> {
            val experts = expertsState.experts
            if (experts.isEmpty()) {
                Box(
                    modifier = modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No experts available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                Column(
                    modifier = modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Our Experts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(onClick = onViewAll) {
                            Text("View All")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(experts.take(5)) { expert ->
                            ExpertCard(
                                expert = expert,
                                storeId = storeId,
                                onExpertClick = { onExpertClick(expert) },
                                modifier = Modifier.width(140.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "All Experts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    experts.forEach { expert ->
                        ExpertListItem(
                            expert = expert,
                            storeId = storeId,
                            onExpertClick = { onExpertClick(expert) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
        else -> {}
    }
}
