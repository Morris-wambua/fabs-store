package com.morrislabs.fabs_store.ui.screens.services

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.data.model.toDisplayName
import com.morrislabs.fabs_store.ui.viewmodel.ServicesViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

private val GreenAccent = Color(0xFF22C55E)
private val LightGreen = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesManagementListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddService: () -> Unit,
    onNavigateToEditService: (String) -> Unit,
    storeViewModel: StoreViewModel = viewModel(),
    servicesViewModel: ServicesViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val servicesState by servicesViewModel.servicesState.collectAsState()

    val storeId = when (storeState) {
        is StoreViewModel.StoreState.Success -> (storeState as StoreViewModel.StoreState.Success).data.id ?: ""
        else -> ""
    }

    LaunchedEffect(storeId) {
        if (storeId.isNotEmpty()) {
            servicesViewModel.fetchServices(storeId)
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Services", "By Category")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Services", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddService,
                containerColor = GreenAccent,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Service",
                    tint = Color.Black
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GreenAccent
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) GreenAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            when (servicesState) {
                is ServicesViewModel.ServicesState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }
                is ServicesViewModel.ServicesState.Error -> {
                    val errorMessage = (servicesState as ServicesViewModel.ServicesState.Error).message
                    ServiceErrorState(errorMessage) {
                        if (storeId.isNotEmpty()) servicesViewModel.fetchServices(storeId)
                    }
                }
                is ServicesViewModel.ServicesState.Success -> {
                    val services = (servicesState as ServicesViewModel.ServicesState.Success).services

                    if (services.isEmpty()) {
                        ServiceEmptyState(onAddService = onNavigateToAddService)
                    } else {
                        when (selectedTab) {
                            0 -> AllServicesTab(services, onNavigateToEditService)
                            1 -> ByCategoryTab(services, onNavigateToEditService)
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }
            }
        }
    }
}

@Composable
private fun AllServicesTab(
    services: List<TypeOfServiceDTO>,
    onEditService: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(services, key = { it.id }) { service ->
            ServiceCard(service = service, onEdit = { onEditService(service.id) })
        }
    }
}

@Composable
private fun ByCategoryTab(
    services: List<TypeOfServiceDTO>,
    onEditService: (String) -> Unit
) {
    val grouped = services.groupBy { it.mainCategory }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        grouped.forEach { (category, categoryServices) ->
            item {
                Text(
                    text = category.name.replace("_", " "),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(categoryServices, key = { it.id }) { service ->
                ServiceCard(service = service, onEdit = { onEditService(service.id) })
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: TypeOfServiceDTO,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {
                if (service.imageUrl != null) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = service.subCategory.toDisplayName(),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = service.subCategory.toDisplayName().take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenAccent
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.subCategory.toDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "KES ${service.price} \u2022 ${service.duration ?: 60} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.mainCategory.toDisplayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = GreenAccent
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onEdit)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ServiceEmptyState(onAddService: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No services yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your first service to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onAddService)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    "Add Service",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ServiceErrorState(errorMessage: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Error: $errorMessage")
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
