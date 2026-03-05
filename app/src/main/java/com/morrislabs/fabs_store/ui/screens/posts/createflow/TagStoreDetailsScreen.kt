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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class MockStore(val id: String, val name: String)

@Composable
fun TagStoreDetailsScreen(
    viewModel: CreatePostFlowViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    var search by remember { mutableStateOf("") }

    val stores = remember {
        listOf(
            MockStore("store_001", "Smith Ltd Beauty Bar"),
            MockStore("store_002", "Glow Lounge Spa"),
            MockStore("store_003", "Prime Cut Barbers")
        )
    }

    val filtered = stores.filter { it.name.contains(search, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FDF6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Tag Store Details",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.padding(end = 48.dp))
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Find a store by name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF13EC5B),
                unfocusedBorderColor = Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(14.dp)
        )

        draft.taggedStore?.let { tagged ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(Color(0xFFECF8E8), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0x3313EC5B), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF13EC5B), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.Black)
                    }
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(tagged.storeName, fontWeight = FontWeight.Bold)
                        Text("TAGGED STORE", color = Color(0xFF6B7280))
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { store ->
                val selected = draft.taggedStore?.storeId == store.id
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
                        .clickable {
                            viewModel.setTaggedStore(
                                TaggedStoreInfo(
                                    storeId = store.id,
                                    storeName = store.name,
                                    selectedServiceIds = draft.taggedStore?.selectedServiceIds ?: emptyList()
                                )
                            )
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x2213EC5B), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF13EC5B))
                    }
                    Text(
                        text = store.name,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp)
                    )
                    if (selected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF13EC5B))
                    }
                }
            }
        }

        Button(
            onClick = {
                if (draft.taggedStore == null && stores.isNotEmpty()) {
                    val first = stores.first()
                    viewModel.setTaggedStore(TaggedStoreInfo(first.id, first.name))
                }
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13EC5B), contentColor = Color.Black)
        ) {
            Text("Select Services", fontWeight = FontWeight.Bold)
        }
    }
}
