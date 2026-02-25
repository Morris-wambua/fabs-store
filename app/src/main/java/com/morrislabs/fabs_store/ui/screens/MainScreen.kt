package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.FetchStoreResponse
import com.morrislabs.fabs_store.data.model.PostDTO
import com.morrislabs.fabs_store.data.model.ReservationFilter
import com.morrislabs.fabs_store.ui.screens.dashboard.DashboardScreen
import com.morrislabs.fabs_store.ui.screens.dashboard.buildChecklistSteps
import com.morrislabs.fabs_store.ui.screens.posts.StorePostsScreen
import com.morrislabs.fabs_store.ui.viewmodel.ExpertViewModel
import com.morrislabs.fabs_store.ui.viewmodel.PostViewModel
import com.morrislabs.fabs_store.ui.viewmodel.ReviewViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

private enum class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    RESERVATIONS("Reservations", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    POSTS("Posts", Icons.Filled.GridView, Icons.Outlined.GridView),
    EXPERTS("Experts", Icons.Filled.People, Icons.Outlined.People)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    store: FetchStoreResponse,
    storeViewModel: StoreViewModel,
    expertViewModel: ExpertViewModel,
    postViewModel: PostViewModel,
    reviewViewModel: ReviewViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateExpert: (String) -> Unit,
    onNavigateToExpertDetails: (String) -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToDailySchedule: () -> Unit,
    onNavigateToStoreProfile: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    onLogout: () -> Unit
) {
    val storeId = store.id ?: ""
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val reservationsState by storeViewModel.reservationsState.collectAsState()
    val isRefreshing by storeViewModel.isRefreshing.collectAsState()
    val expertsState by expertViewModel.expertsState.collectAsState()
    val postsState by postViewModel.postsState.collectAsState()
    val isPostsRefreshing by postViewModel.isRefreshing.collectAsState()
    val reviewsState by reviewViewModel.reviewsState.collectAsState()
    var selectedReservationFilter by rememberSaveable { mutableStateOf(ReservationFilter.PENDING_APPROVAL) }

    val postsResolved = postsState is StoreViewModel.LoadingState.Success || postsState is StoreViewModel.LoadingState.Error
    val hasPosts = when (postsState) {
        is StoreViewModel.LoadingState.Success ->
            (postsState as StoreViewModel.LoadingState.Success<List<PostDTO>>).data.isNotEmpty()
        else -> false
    }
    val checklistSteps = buildChecklistSteps(store = store, hasPosts = hasPosts)
    val showChecklist = postsResolved && checklistSteps.any { !it.isCompleted }

    LaunchedEffect(storeId) {
        if (storeId.isNotEmpty()) {
            expertViewModel.getExpertsByStoreId(storeId)
            postViewModel.fetchStorePosts(storeId)
            reviewViewModel.fetchStoreReviews(storeId)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp
            ) {
                BottomNavItem.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(text = item.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    DashboardScreen(
                        store = store,
                        reservationsState = reservationsState,
                        reviewsState = reviewsState,
                        checklistSteps = checklistSteps,
                        showChecklist = showChecklist,
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            storeViewModel.fetchUserStore()
                            if (storeId.isNotEmpty()) {
                                storeViewModel.refreshReservations(storeId)
                                expertViewModel.getExpertsByStoreId(storeId)
                                reviewViewModel.fetchStoreReviews(storeId)
                            }
                        },
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToCreateExpert = onNavigateToCreateExpert,
                        onNavigateToServices = onNavigateToServices,
                        onNavigateToDailySchedule = onNavigateToDailySchedule,
                        onNavigateToReservations = { selectedTab = 1 },
                        onNavigateToReviews = onNavigateToReviews,
                        onNavigateToChecklist = onNavigateToChecklist
                    )
                }
                1 -> {
                    ReservationsTabContent(
                        storeId = storeId,
                        storeViewModel = storeViewModel,
                        reservationsState = reservationsState,
                        isRefreshing = isRefreshing,
                        selectedFilter = selectedReservationFilter,
                        onFilterChange = { newFilter ->
                            selectedReservationFilter = newFilter
                            val filterStatus = when (newFilter) {
                                ReservationFilter.AWAITING_PAYMENT -> "BOOKED_PENDING_PAYMENT"
                                ReservationFilter.PENDING_APPROVAL -> "PENDING_APPROVAL"
                                ReservationFilter.UPCOMING -> "BOOKED_ACCEPTED"
                                ReservationFilter.IN_PROGRESS -> "ACTIVE_SERVICE"
                                ReservationFilter.CANCELLED -> "CANCELLED"
                                ReservationFilter.COMPLETED -> "SERVED"
                                ReservationFilter.LAPSED_PAID -> "LAPSED_PAID"
                                ReservationFilter.LAPSED_NOT_ACCEPTED -> "LAPSED_NOT_ACCEPTED"
                            }
                            storeViewModel.fetchReservations(storeId, filterStatus)
                        }
                    )
                }
                2 -> {
                    StorePostsScreen(
                        posts = postsState,
                        isRefreshing = isPostsRefreshing,
                        onRefresh = { postViewModel.refreshPosts(storeId) },
                        onCreatePost = onNavigateToCreatePost,
                        onPostClick = onNavigateToPostDetail
                    )
                }
                3 -> {
                    ExpertsTabContent(
                        expertsState = expertsState,
                        storeId = storeId,
                        onExpertClick = { expert -> onNavigateToExpertDetails(expert.id) },
                        onViewAll = {},
                        onRetry = {
                            if (storeId.isNotEmpty()) {
                                expertViewModel.getExpertsByStoreId(storeId)
                            }
                        },
                        onCreateExpert = { onNavigateToCreateExpert(storeId) }
                    )
                }
            }
        }
    }
}
