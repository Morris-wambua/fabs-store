package com.morrislabs.fabs_store.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.ReservationFilter
import com.morrislabs.fabs_store.data.model.ReservationStatus
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ReservationDetailsScreen(
    reservation: ReservationWithPaymentDTO,
    selectedFilter: ReservationFilter,
    onApproveReservation: (String) -> Unit,
    onRejectReservation: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val customerPhone = reservation.customerPhone.orEmpty().trim()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Booking Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            IconButton(
                onClick = {},
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StatusPill(reservation.status)
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val customerName = normalizeCustomerName(reservation.name)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(58.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = customerName.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(customerName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = customerPhone.ifBlank { "No phone available" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = if (customerPhone.isNotBlank()) {
                                    Modifier.combinedClickable(
                                        onClick = {
                                            openDialer(context, customerPhone)
                                        },
                                        onLongClick = {
                                            copyPhoneToClipboard(context, customerPhone)
                                        }
                                    )
                                } else {
                                    Modifier
                                }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (customerPhone.isNotBlank()) {
                                    openDialer(context, customerPhone)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = customerPhone.isNotBlank()
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Call")
                        }
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Message")
                        }
                    }
                }
            }

            DetailCard(
                icon = Icons.Default.Spa,
                label = "SERVICE",
                title = reservation.typeOfServiceName.ifBlank { "Service" },
                subtitle = "${reservation.startTime} - ${reservation.endTime}"
            )

            DetailCard(
                icon = Icons.Default.Person,
                label = "ASSIGNED EXPERT",
                title = reservation.reservationExpertName.ifBlank { "Not Assigned" },
                subtitle = "Service Expert"
            )

            Text(
                "BOOKING TIMELINE",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box {
                Box(
                    modifier = Modifier
                        .padding(start = 3.dp, top = 8.dp)
                        .width(2.dp)
                        .height(116.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                            )
                        )
                )
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    TimelineRow(title = "Booking Created", subtitle = "${reservation.reservationDate} - ${reservation.startTime}", active = true)
                    TimelineRow(
                        title = "Awaiting Confirmation",
                        subtitle = "Pending store owner action",
                        active = reservation.status == ReservationStatus.BOOKED_PENDING_PAYMENT ||
                                reservation.status == ReservationStatus.BOOKED_PENDING_ACCEPTANCE
                    )
                    TimelineRow(
                        title = "Session Start",
                        subtitle = "Scheduled for ${reservation.reservationDate}, ${reservation.startTime}",
                        active = reservation.status == ReservationStatus.BOOKED_ACCEPTED ||
                                reservation.status == ReservationStatus.IN_PROGRESS ||
                                reservation.status == ReservationStatus.PENDING_FINAL_PAYMENT
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "PAYMENT SUMMARY",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PaymentRow("Subtotal", "KES ${reservation.price.toInt()}")
                    PaymentRow("Service Fee", "KES 0")
                    PaymentRow("Tax (8%)", "KES 0")
                    HorizontalDivider()
                    PaymentRow("Total", "KES ${reservation.price.toInt()}", bold = true)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                selectedFilter == ReservationFilter.LAPSED_NOT_ACCEPTED -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Contact")
                        }
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.weight(2f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Approve Booking")
                        }
                    }
                }
                reservation.status == ReservationStatus.CANCELLED -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), enabled = false) {
                            Text("Reject")
                        }
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.weight(2f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Approve Booking")
                        }
                    }
                }
                else -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                onRejectReservation(reservation.id)
                                onNavigateBack()
                            },
                            enabled = reservation.status == ReservationStatus.BOOKED_PENDING_PAYMENT ||
                                    reservation.status == ReservationStatus.BOOKED_PENDING_ACCEPTANCE ||
                                    reservation.status == ReservationStatus.BOOKED_ACCEPTED,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = {
                                onApproveReservation(reservation.id)
                                onNavigateBack()
                            },
                            enabled = reservation.status == ReservationStatus.BOOKED_PENDING_ACCEPTANCE,
                            modifier = Modifier.weight(2f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Approve Booking")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusPill(status: ReservationStatus) {
    val (text, bg, fg) = when (status) {
        ReservationStatus.BOOKED_PENDING_PAYMENT -> Triple("Awaiting Payment", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        ReservationStatus.BOOKED_PENDING_ACCEPTANCE -> Triple("Pending Approval", MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.colorScheme.primary)
        ReservationStatus.BOOKED_ACCEPTED -> Triple("Upcoming", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        ReservationStatus.SERVED -> Triple("Completed", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        ReservationStatus.CANCELLED -> Triple("Cancelled", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        ReservationStatus.IN_PROGRESS -> Triple("In Progress", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        ReservationStatus.PENDING_FINAL_PAYMENT -> Triple("Pending Final Payment", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(7.dp).background(fg, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, color = fg, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun DetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TimelineRow(title: String, subtitle: String, active: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(
                    if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PaymentRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = if (bold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold) else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun normalizeCustomerName(raw: String): String {
    val cleaned = raw
        .removePrefix("Appointment of +")
        .removePrefix("Appointment of")
        .trim()
    return if (cleaned.isBlank()) raw else cleaned
}

private fun openDialer(context: Context, phone: String) {
    val cleanedPhone = phone.trim()
    if (cleanedPhone.isBlank()) {
        return
    }
    context.startActivity(
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(cleanedPhone)}"))
    )
}

private fun copyPhoneToClipboard(context: Context, phone: String) {
    val cleanedPhone = phone.trim()
    if (cleanedPhone.isBlank()) {
        return
    }
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText("customer_phone", cleanedPhone))
    Toast.makeText(context, "Phone copied to clipboard", Toast.LENGTH_SHORT).show()
}
