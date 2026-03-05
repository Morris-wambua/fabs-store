package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class MockService(
    val id: String,
    val name: String,
    val duration: String,
    val price: String,
    val icon: ImageVector,
    val tint: Color
)

@Composable
fun TagStoreServicesScreen(
    viewModel: CreatePostFlowViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val services = remember {
        listOf(
            MockService("svc_1", "Classic Haircut", "45 mins", "$45.00", Icons.Default.ContentCut, Color(0xFFFCA5A5)),
            MockService("svc_2", "Dermaplaning", "60 mins", "$85.00", Icons.Default.Face, Color(0xFFA7F3D0)),
            MockService("svc_3", "Express Facial", "30 mins", "$55.00", Icons.Default.Face, Color(0xFFBFDBFE)),
            MockService("svc_4", "Brow Shaping", "20 mins", "$25.00", Icons.Default.ContentCut, Color(0xFFFDE68A)),
            MockService("svc_5", "Gel Manicure", "45 mins", "$40.00", Icons.Default.Face, Color(0xFFE9D5FF))
        )
    }

    var storeQuery by remember { mutableStateOf("") }
    var serviceQuery by remember { mutableStateOf("") }
    val selectedServiceIds = remember {
        mutableStateListOf<String>().apply { addAll(draft.taggedStore?.selectedServiceIds ?: emptyList()) }
    }

    val taggedStore = draft.taggedStore ?: TaggedStoreInfo(
        storeId = "store_001",
        storeName = "Smith Ltd Beauty Bar",
        selectedServiceIds = selectedServiceIds
    )

    val filteredServices = services.filter {
        it.name.contains(serviceQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F6))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Tag Store & Services",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.padding(end = 48.dp))
        }

        OutlinedTextField(
            value = storeQuery,
            onValueChange = { storeQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Find a store by name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF13EC5B),
                unfocusedBorderColor = Color(0xFFE5E7EB)
            )
        )

        StoreCard(
            taggedStore = taggedStore,
            selectedServiceIds = selectedServiceIds,
            services = services,
            onRemoveService = { selectedServiceIds.remove(it) }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Select Services", fontWeight = FontWeight.Bold)
            Text("${services.size} available", color = Color(0xFF6B7280))
        }

        OutlinedTextField(
            value = serviceQuery,
            onValueChange = { serviceQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search services...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF13EC5B),
                unfocusedBorderColor = Color(0xFFE5E7EB)
            )
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredServices) { service ->
                val selected = selectedServiceIds.contains(service.id)
                ServiceRow(
                    service = service,
                    selected = selected,
                    onToggle = {
                        if (selected) selectedServiceIds.remove(service.id) else selectedServiceIds.add(service.id)
                    }
                )
            }
        }

        Button(
            onClick = {
                viewModel.setTaggedStore(
                    taggedStore.copy(selectedServiceIds = selectedServiceIds.toList())
                )
                onDone()
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13EC5B), contentColor = Color.Black),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Text(" Done", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StoreCard(
    taggedStore: TaggedStoreInfo,
    selectedServiceIds: List<String>,
    services: List<MockService>,
    onRemoveService: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF13EC5B), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.Black)
            }
            Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                Text(taggedStore.storeName, fontWeight = FontWeight.Bold)
                Text("TAGGED STORE", color = Color(0xFF6B7280))
            }
            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF6B7280))
        }

        Text(
            text = "SELECTED SERVICES",
            color = Color(0xFF16A34A),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            selectedServiceIds.forEach { id ->
                val label = services.firstOrNull { it.id == id }?.name ?: id
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(99.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(99.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = Color(0xFF111827))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.padding(start = 4.dp).size(14.dp).clickable { onRemoveService(id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceRow(
    service: MockService,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF13EC5B) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onToggle)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(service.tint, CircleShape)
                .padding(12.dp)
        ) {
            Icon(service.icon, contentDescription = null, tint = Color(0xFF111827))
        }
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Text(service.name, fontWeight = FontWeight.Bold)
            Text("${service.duration} . ${service.price}", color = Color(0xFF6B7280))
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color(0xFF13EC5B), CircleShape)
                    .background(Color(0xFF13EC5B), CircleShape)
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
            }
        } else {
            Box(modifier = Modifier.border(1.dp, Color(0xFFD1D5DB), CircleShape).padding(10.dp))
        }
    }
}
