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

internal val quickDurations = listOf(30, 45, 60)
internal val extendedDurations = listOf(90, 120, 180, 240, 300, 360, 420, 480, 540)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ServiceSelectionSection(
    serviceSearch: String,
    onServiceSearchChange: (String) -> Unit,
    servicesState: StoreViewModel.LoadingState<List<TypeOfServiceDTO>>,
    services: List<TypeOfServiceDTO>,
    selectedServiceIds: Set<String>,
    onServiceToggled: (TypeOfServiceDTO) -> Unit
) {
    Text(
        text = "SELECT SERVICES",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = serviceSearch,
        onValueChange = onServiceSearchChange,
        placeholder = { Text("Search services and sub-categories...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
    Spacer(modifier = Modifier.height(10.dp))
    when (servicesState) {
        is StoreViewModel.LoadingState.Loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
        is StoreViewModel.LoadingState.Error -> Text(text = servicesState.message, color = MaterialTheme.colorScheme.error)
        else -> Unit
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        services.forEach { service ->
            val selected = selectedServiceIds.contains(service.id)
            val label = "${service.subCategory.toDisplayName()} • KES ${service.price}"
            FilterChip(
                selected = selected,
                onClick = { onServiceToggled(service) },
                label = { Text(label) },
                leadingIcon = if (selected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExpertAssignmentSection(
    expertsState: StoreViewModel.LoadingState<List<ExpertDTO>>,
    selectedServices: List<TypeOfServiceDTO>,
    experts: List<ExpertDTO>,
    selectedExpertsByService: Map<String, Set<String>>,
    onExpertToggled: (String, String) -> Unit
) {
    Text(
        text = "ASSIGN EXPERTS PER SERVICE",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    when (expertsState) {
        is StoreViewModel.LoadingState.Loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
        is StoreViewModel.LoadingState.Error -> Text(text = expertsState.message, color = MaterialTheme.colorScheme.error)
        else -> Unit
    }
    if (selectedServices.isEmpty()) {
        Text("Select at least one service first")
        return
    }
    selectedServices.forEach { service ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 1.dp
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "${service.subCategory.toDisplayName()} • KES ${service.price}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    experts.forEach { expert ->
                        val selectedForService = selectedExpertsByService[service.id].orEmpty()
                        val selected = selectedForService.contains(expert.id)
                        FilterChip(
                            selected = selected,
                            onClick = { onExpertToggled(service.id, expert.id) },
                            label = { Text(expert.name) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DurationSection(
    selectedDurationMinutes: Int?,
    onQuickDurationSelected: (Int) -> Unit,
    onOtherClick: () -> Unit
) {
    Text(
        text = "DURATION",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        quickDurations.forEach { duration ->
            Surface(
                onClick = { onQuickDurationSelected(duration) },
                modifier = Modifier.width(84.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = if (selectedDurationMinutes == duration) 2.dp else 1.dp,
                    color = if (selectedDurationMinutes == duration) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(formatDuration(duration), fontWeight = FontWeight.Bold)
                }
            }
        }
        Surface(
            onClick = onOtherClick,
            modifier = Modifier.width(84.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = if (selectedDurationMinutes in extendedDurations) 2.dp else 1.dp,
                color = if (selectedDurationMinutes in extendedDurations) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        ) {
            Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text("Other", fontWeight = FontWeight.Bold)
            }
        }
    }
    if (selectedDurationMinutes in extendedDurations) {
        Spacer(modifier = Modifier.height(6.dp))
        Text("Selected: ${formatDuration(selectedDurationMinutes ?: 0)}")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TimeSlotAssignmentSection(
    selectedServices: List<TypeOfServiceDTO>,
    expertsById: Map<String, ExpertDTO>,
    selectedExpertsByService: Map<String, Set<String>>,
    availableSlotsByExpertState: Map<String, StoreViewModel.LoadingState<List<TimeSlot>>>,
    selectedTimeSlotsByPair: Map<String, TimeSlot>,
    onTimeSlotSelected: (serviceId: String, expertId: String, slot: TimeSlot) -> Unit
) {
    Text(
        text = "SELECT TIME SLOTS",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (selectedServices.isEmpty()) {
        Text("Select services and experts first")
        return
    }

    selectedServices.forEach { service ->
        val selectedExpertIds = selectedExpertsByService[service.id].orEmpty()
        if (selectedExpertIds.isEmpty()) {
            return@forEach
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 1.dp
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = service.subCategory.toDisplayName(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                selectedExpertIds.forEach { expertId ->
                    val expertName = expertsById[expertId]?.name ?: "Expert"
                    Text(
                        text = expertName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    when (val state = availableSlotsByExpertState[expertId]) {
                        is StoreViewModel.LoadingState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                        is StoreViewModel.LoadingState.Error -> {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                        is StoreViewModel.LoadingState.Success -> {
                            val slots = state.data
                            if (slots.isEmpty()) {
                                Text("No available slots")
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    slots.forEach { slot ->
                                        val key = pairKey(service.id, expertId)
                                        val selectedSlot = selectedTimeSlotsByPair[key]
                                        val selected = selectedSlot == slot
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onTimeSlotSelected(service.id, expertId, slot) },
                                            label = { Text(formatTimeLabel(slot.startTime)) },
                                            leadingIcon = if (selected) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            Text("Select date, duration, and experts to load slots")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
internal fun ReadOnlyPriceSection(totalPrice: Int) {
    Text(
        text = "BOOKING PRICE (KES)",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = if (totalPrice > 0) totalPrice.toString() else "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        readOnly = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
        placeholder = { Text("0") },
        shape = RoundedCornerShape(12.dp)
    )
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
