package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.data.model.TimeSlot
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.data.model.toDisplayName
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
internal fun WalkInStepProgress(currentStep: Int, modifier: Modifier = Modifier) {
    val steps = listOf("Customer", "Services", "Schedule")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        steps.forEachIndexed { index, label ->
            val reached = index <= currentStep
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (reached) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${index + 1}. $label",
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (reached) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ServiceSelectionStep(
    serviceSearch: String,
    onServiceSearchChange: (String) -> Unit,
    servicesState: StoreViewModel.LoadingState<List<TypeOfServiceDTO>>,
    services: List<TypeOfServiceDTO>,
    selectedServiceIds: Set<String>,
    onServiceToggled: (TypeOfServiceDTO) -> Unit
) {
    OutlinedTextField(
        value = serviceSearch,
        onValueChange = onServiceSearchChange,
        placeholder = { Text("Search by service name or category") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Spacer(modifier = Modifier.height(12.dp))

    when (servicesState) {
        is StoreViewModel.LoadingState.Loading -> CircularProgressIndicator(modifier = Modifier.size(22.dp))
        is StoreViewModel.LoadingState.Error -> Text(servicesState.message, color = MaterialTheme.colorScheme.error)
        else -> Unit
    }

    if (services.isEmpty()) {
        Text("No services found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        services.forEach { service ->
            val selected = selectedServiceIds.contains(service.id)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                tonalElevation = if (selected) 2.dp else 0.dp,
                border = BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                ),
                onClick = { onServiceToggled(service) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = service.subCategory.toDisplayName(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "KES ${service.price} • ${formatDuration(service.duration ?: 60)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExpertAssignmentStep(
    expertsState: StoreViewModel.LoadingState<List<ExpertDTO>>,
    selectedServices: List<TypeOfServiceDTO>,
    experts: List<ExpertDTO>,
    selectedExpertsByService: Map<String, Set<String>>,
    onExpertToggled: (String, String) -> Unit
) {
    when (expertsState) {
        is StoreViewModel.LoadingState.Loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
        is StoreViewModel.LoadingState.Error -> Text(expertsState.message, color = MaterialTheme.colorScheme.error)
        else -> Unit
    }

    selectedServices.forEach { service ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 1.dp
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = service.subCategory.toDisplayName(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${formatDuration(service.duration ?: 60)} • KES ${service.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    experts.forEach { expert ->
                        val selected = selectedExpertsByService[service.id].orEmpty().contains(expert.id)
                        FilterChip(
                            selected = selected,
                            onClick = { onExpertToggled(service.id, expert.id) },
                            label = { Text(expert.name) },
                            leadingIcon = if (selected) {
                                {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TimeSlotAssignmentStep(
    selectedServices: List<TypeOfServiceDTO>,
    expertsById: Map<String, ExpertDTO>,
    selectedExpertsByService: Map<String, Set<String>>,
    availableSlotsByPairState: Map<String, StoreViewModel.LoadingState<List<TimeSlot>>>,
    selectedTimeSlotsByPair: Map<String, TimeSlot>,
    onTimeSlotSelected: (serviceId: String, expertId: String, slot: TimeSlot) -> Unit
) {
    selectedServices.forEach { service ->
        val expertIds = selectedExpertsByService[service.id].orEmpty()
        if (expertIds.isEmpty()) {
            return@forEach
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 1.dp
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = service.subCategory.toDisplayName(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Fixed duration: ${formatDuration(service.duration ?: 60)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                expertIds.forEach { expertId ->
                    val expertName = expertsById[expertId]?.name ?: "Expert"
                    val pairKey = pairKey(service.id, expertId)
                    Text(
                        text = expertName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    when (val state = availableSlotsByPairState[pairKey]) {
                        is StoreViewModel.LoadingState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }

                        is StoreViewModel.LoadingState.Error -> {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }

                        is StoreViewModel.LoadingState.Success -> {
                            if (state.data.isEmpty()) {
                                Text("No available slots", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.data.forEach { slot ->
                                        val selected = selectedTimeSlotsByPair[pairKey] == slot
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onTimeSlotSelected(service.id, expertId, slot) },
                                            label = { Text(formatTimeLabel(slot.startTime)) },
                                            leadingIcon = if (selected) {
                                                {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }

                        else -> Text("Select date and experts to load slots")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
internal fun WalkInBookingTotals(totalPrice: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Booking Price",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "KES $totalPrice",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

internal fun formatDuration(minutes: Int): String {
    if (minutes < 60) return "$minutes min"
    if (minutes % 60 == 0) {
        val hours = minutes / 60
        return if (hours == 1) "1 hr" else "$hours hrs"
    }
    return "$minutes min"
}

internal fun pairKey(serviceId: String, expertId: String): String = "$serviceId|$expertId"

private fun formatTimeLabel(timeString: String): String {
    return try {
        val input = DateTimeFormatter.ofPattern("HH:mm")
        val output = DateTimeFormatter.ofPattern("h:mm a")
        LocalTime.parse(timeString, input).format(output)
    } catch (_: Exception) {
        timeString
    }
}
