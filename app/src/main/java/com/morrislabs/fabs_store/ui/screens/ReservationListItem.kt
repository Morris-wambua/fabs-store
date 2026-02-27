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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.ReservationFilter
import com.morrislabs.fabs_store.data.model.ReservationTransitionAction
import com.morrislabs.fabs_store.data.model.ReservationStatus
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO

@Composable
internal fun ReservationRow(
    reservation: ReservationWithPaymentDTO,
    selectedFilter: ReservationFilter,
    onDetailsClick: () -> Unit,
    onTransitionClick: (String, ReservationTransitionAction) -> Unit,
    onMessageCustomer: (userId: String, customerName: String) -> Unit = { _, _ -> }
) {
    val customerName = normalizeCustomerName(reservation.name)
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "expand"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = customerName.take(2).uppercase(),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = reservation.typeOfServiceName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(reservation.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        reservation.reservationDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        reservation.startTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "KES ${reservation.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when {
                            selectedFilter == ReservationFilter.AWAITING_PAYMENT -> {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Call customer to confirm this booking",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                            selectedFilter == ReservationFilter.LAPSED_NOT_ACCEPTED -> {
                                OutlinedButton(
                                    onClick = {
                                        val userId = reservation.userId
                                        if (userId != null) {
                                            onMessageCustomer(userId, normalizeCustomerName(reservation.name))
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp),
                                    enabled = reservation.userId != null
                                ) {
                                    Icon(
                                        Icons.Default.ChatBubble,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Contact")
                                }
                            }
                            reservation.status == ReservationStatus.BOOKED_PENDING_ACCEPTANCE -> {
                                Button(
                                    onClick = {
                                        onTransitionClick(
                                            reservation.id,
                                            ReservationTransitionAction.STORE_APPROVE_BOOKING
                                        )
                                    },
                                    enabled = reservation.minimumPaymentMet == true,
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text(if (reservation.minimumPaymentMet == true) "Approve" else "Awaiting Payment")
                                }
                            }
                            reservation.status == ReservationStatus.BOOKED_ACCEPTED -> {
                                Button(
                                    onClick = {
                                        onTransitionClick(
                                            reservation.id,
                                            ReservationTransitionAction.STORE_ACCEPT_SESSION
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text("Accept Session")
                                }
                            }
                            reservation.status == ReservationStatus.IN_PROGRESS ||
                                reservation.status == ReservationStatus.PENDING_FINAL_PAYMENT -> {
                                Button(
                                    onClick = {
                                        onTransitionClick(
                                            reservation.id,
                                            ReservationTransitionAction.STORE_COMPLETE_SERVICE
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text(
                                        if ((reservation.outstandingBalance ?: 0.0) > 0.0) {
                                            "Request Settlement"
                                        } else {
                                            "Complete Service"
                                        }
                                    )
                                }
                            }
                            else -> Spacer(modifier = Modifier.weight(1f))
                        }

                        OutlinedButton(
                            onClick = onDetailsClick,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text("Details")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun normalizeCustomerName(raw: String): String {
    val cleaned = raw
        .removePrefix("Appointment of +")
        .removePrefix("Appointment of")
        .trim()
    return if (cleaned.isBlank()) raw else cleaned
}

@Composable
private fun StatusBadge(status: ReservationStatus) {
    val (bg, fg, label) = when (status) {
        ReservationStatus.BOOKED_PENDING_ACCEPTANCE -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            MaterialTheme.colorScheme.primary,
            "PENDING"
        )
        ReservationStatus.BOOKED_PENDING_PAYMENT -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "AWAITING PAYMENT"
        )
        ReservationStatus.BOOKED_ACCEPTED -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "UPCOMING"
        )
        ReservationStatus.SERVED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "COMPLETED"
        )
        ReservationStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "CANCELLED"
        )
        ReservationStatus.IN_PROGRESS -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "IN PROGRESS"
        )
        ReservationStatus.PENDING_FINAL_PAYMENT -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "PENDING SETTLEMENT"
        )
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
