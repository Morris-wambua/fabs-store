package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StoreMallDirectory
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit = {},
    onNavigateToEmployees: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCreateStore: () -> Unit = {},
    onLogout: () -> Unit = {},
    storeViewModel: StoreViewModel = viewModel()
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    HeaderSection(
                        storeName = store.name,
                        onLogout = onLogout,
                        onSettings = onNavigateToSettings
                    )

                    QuickStatsSection(
                        modifier = Modifier.padding(16.dp)
                    )

                    ManagementCardsSection(
                        onNavigateToReservations = onNavigateToReservations,
                        onNavigateToEmployees = onNavigateToEmployees,
                        onNavigateToServices = onNavigateToServices,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    RecentActivitySection(
                        modifier = Modifier.padding(16.dp)
                    )

                    Box(modifier = Modifier.height(24.dp))
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
            title = "Employees",
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
