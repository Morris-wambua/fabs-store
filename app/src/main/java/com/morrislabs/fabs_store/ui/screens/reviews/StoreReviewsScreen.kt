package com.morrislabs.fabs_store.ui.screens.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.ReviewDTO
import com.morrislabs.fabs_store.ui.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreReviewsScreen(
    storeId: String,
    onNavigateBack: () -> Unit,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val reviewsState by reviewViewModel.reviewsState.collectAsState()
    val replyState by reviewViewModel.replyState.collectAsState()
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var showReplyDialog by remember { mutableStateOf(false) }
    var replyingToReview by remember { mutableStateOf<ReviewDTO?>(null) }
    var replyText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(storeId) {
        reviewViewModel.fetchStoreReviews(storeId)
    }

    LaunchedEffect(replyState) {
        when (replyState) {
            is ReviewViewModel.ReplyState.Success -> {
                showReplyDialog = false
                replyText = ""
                replyingToReview = null
                snackbarHostState.showSnackbar("Reply sent successfully")
                reviewViewModel.resetReplyState()
            }
            is ReviewViewModel.ReplyState.Error -> {
                snackbarHostState.showSnackbar(
                    (replyState as ReviewViewModel.ReplyState.Error).message
                )
                reviewViewModel.resetReplyState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Store Reviews",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = reviewsState) {
            is ReviewViewModel.ReviewsState.Idle,
            is ReviewViewModel.ReviewsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is ReviewViewModel.ReviewsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { reviewViewModel.fetchStoreReviews(storeId) }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is ReviewViewModel.ReviewsState.Success -> {
                val reviews = state.reviews
                val summary = state.summary

                if (reviews.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No store reviews yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val unrepliedCount = reviews.count { it.storeReply.isNullOrBlank() }

                    val filteredReviews = when (selectedFilter) {
                        "All" -> reviews
                        "Unreplied" -> reviews.filter { it.storeReply.isNullOrBlank() }
                        else -> {
                            val starCount = selectedFilter.split(" ")
                                .firstOrNull()?.toIntOrNull()
                            if (starCount != null) {
                                reviews.filter { it.rating.toInt() == starCount }
                            } else {
                                reviews
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            ReviewSummaryCard(summary = summary)
                        }

                        item {
                            ReviewFilterChips(
                                selectedFilter = selectedFilter,
                                onFilterSelected = { selectedFilter = it },
                                unrepliedCount = unrepliedCount
                            )
                        }

                        item {
                            Text(
                                text = "Recent Feedback",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(filteredReviews, key = { it.id }) { review ->
                            ReviewFeedbackCard(
                                review = review,
                                formatDate = ::formatRelativeDate,
                                onReply = {
                                    replyingToReview = review
                                    replyText = review.storeReply ?: ""
                                    showReplyDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReplyDialog && replyingToReview != null) {
        ReplyDialog(
            reviewerName = replyingToReview?.displayName
                ?: replyingToReview?.userName ?: "",
            replyText = replyText,
            onReplyTextChange = { replyText = it },
            isLoading = replyState is ReviewViewModel.ReplyState.Loading,
            onDismiss = {
                showReplyDialog = false
                replyText = ""
                replyingToReview = null
            },
            onSend = {
                replyingToReview?.let { review ->
                    reviewViewModel.replyToReview(review.id, replyText, storeId)
                }
            }
        )
    }
}

@Composable
private fun ReplyDialog(
    reviewerName: String,
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reply to $reviewerName",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            OutlinedTextField(
                value = replyText,
                onValueChange = onReplyTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Write your reply...") },
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            )
        },
        confirmButton = {
            Button(
                onClick = onSend,
                enabled = replyText.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}
