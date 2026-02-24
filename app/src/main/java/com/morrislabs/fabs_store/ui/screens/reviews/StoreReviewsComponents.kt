package com.morrislabs.fabs_store.ui.screens.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.morrislabs.fabs_store.data.model.ReviewDTO
import com.morrislabs.fabs_store.data.model.ReviewSummaryDTO
import kotlin.math.roundToInt

@Composable
fun ReviewSummaryCard(
    summary: ReviewSummaryDTO,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", summary.averageRating),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "/5.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    RatingStars(rating = summary.averageRating)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalReviews} total reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column(modifier = Modifier.weight(1f)) {
                    for (rating in 5 downTo 1) {
                        val count = summary.ratingDistribution[rating] ?: 0
                        RatingDistributionBar(
                            rating = rating,
                            count = count,
                            totalCount = summary.totalReviews
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingStars(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Int = 16
) {
    val filledStars = rating.toInt()
    val hasHalfStar = (rating - filledStars) >= 0.25f && (rating - filledStars) < 0.75f
    val adjustedFilled = if ((rating - filledStars) >= 0.75f) filledStars + 1 else filledStars
    val emptyStars = 5 - adjustedFilled - if (hasHalfStar) 1 else 0

    Row(modifier = modifier) {
        repeat(adjustedFilled) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(starSize.dp)
            )
        }
        if (hasHalfStar) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(starSize.dp)
            )
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier.size(starSize.dp)
            )
        }
    }
}

@Composable
fun RatingDistributionBar(
    rating: Int,
    count: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalCount > 0) count.toFloat() / totalCount else 0f
    val percentageText = if (totalCount > 0) "${(percentage * 100).roundToInt()}%" else "0%"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rating",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(12.dp)
        )
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = percentageText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
    }
}

@Composable
fun ReviewFilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    unrepliedCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val filters = listOf("All", "Unreplied", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star")

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            val isSelected = filter == selectedFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (filter == "Unreplied" && unrepliedCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$unrepliedCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun ReviewFeedbackCard(
    review: ReviewDTO,
    formatDate: (Long) -> String,
    onReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (!review.userImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = review.userImageUrl,
                        contentDescription = "User avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val initials = (review.displayName ?: review.userName)
                        .split(" ")
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .joinToString("")

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.displayName ?: review.userName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    RatingStars(rating = review.rating, starSize = 12)
                }

                Text(
                    text = formatDate(review.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            if (!review.storeReply.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Your Reply",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (review.storeReplyDate != null) {
                                Text(
                                    text = formatDate(review.storeReplyDate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${review.storeReply}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (review.storeReply.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReply,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Text(
                            text = "Reply",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Like",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onReply,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = "Edit Reply",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

fun formatRelativeDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
        days < 7 -> "$days day${if (days != 1L) "s" else ""} ago"
        weeks < 4 -> "$weeks week${if (weeks != 1L) "s" else ""} ago"
        else -> {
            val months = days / 30
            if (months < 12) {
                "$months month${if (months != 1L) "s" else ""} ago"
            } else {
                val years = months / 12
                "$years year${if (years != 1L) "s" else ""} ago"
            }
        }
    }
}
