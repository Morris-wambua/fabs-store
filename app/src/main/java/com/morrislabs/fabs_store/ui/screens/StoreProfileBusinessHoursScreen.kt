package com.morrislabs.fabs_store.ui.screens

import android.app.TimePickerDialog
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.BusinessHourDTO
import com.morrislabs.fabs_store.data.model.UpdateStorePayload
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import java.util.Locale

@Composable
fun StoreProfileBusinessHoursScreen(
    onNavigateBack: () -> Unit,
    storeViewModel: StoreViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val updateState by storeViewModel.updateStoreState.collectAsState()
    var initialized by remember { mutableStateOf(false) }
    var schedules by remember { mutableStateOf(defaultWeeklySchedule()) }
    var storeId by remember { mutableStateOf<String?>(null) }
    var saveRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        storeViewModel.fetchUserStore()
    }

    LaunchedEffect(storeState) {
        if (storeState is StoreViewModel.StoreState.Success && !initialized) {
            val store = (storeState as StoreViewModel.StoreState.Success).data
            storeId = store.id
            schedules = store.businessHours ?: defaultWeeklySchedule()
            initialized = true
        }
    }

    LaunchedEffect(updateState, saveRequested) {
        if (!saveRequested) {
            return@LaunchedEffect
        }
        when (updateState) {
            is StoreViewModel.UpdateStoreState.Success -> {
                saveRequested = false
                storeViewModel.resetUpdateStoreState()
                onNavigateBack()
            }
            is StoreViewModel.UpdateStoreState.Error -> {
                saveRequested = false
                storeViewModel.resetUpdateStoreState()
            }
            else -> Unit
        }
    }

    val isSaving = updateState is StoreViewModel.UpdateStoreState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Business Hours",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "WEEKLY SCHEDULE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = {
                        val monday = schedules.firstOrNull { it.dayIndex == 0 } ?: return@OutlinedButton
                        schedules = schedules.map {
                            it.copy(isOpen = monday.isOpen, openTime = monday.openTime, closeTime = monday.closeTime)
                        }
                    },
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Monday to all")
                }
            }

            schedules.sortedBy { it.dayIndex }.forEach { day ->
                DayHoursCard(
                    day = day,
                    onToggle = { checked ->
                        schedules = schedules.map {
                            if (it.dayIndex == day.dayIndex) it.copy(isOpen = checked) else it
                        }
                    },
                    onOpenTimeChange = { openTime ->
                        schedules = schedules.map {
                            if (it.dayIndex == day.dayIndex) it.copy(openTime = openTime) else it
                        }
                    },
                    onCloseTimeChange = { closeTime ->
                        schedules = schedules.map {
                            if (it.dayIndex == day.dayIndex) it.copy(closeTime = closeTime) else it
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = {
                val id = storeId ?: return@Button
                saveRequested = true
                storeViewModel.updateStore(
                    id,
                    UpdateStorePayload(
                        businessHours = schedules.map { day ->
                            day.copy(
                                openTime = if (day.isOpen) day.openTime else null,
                                closeTime = if (day.isOpen) day.closeTime else null
                            )
                        }
                    )
                )
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Hours", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun DayHoursCard(
    day: BusinessHourDTO,
    onToggle: (Boolean) -> Unit,
    onOpenTimeChange: (String) -> Unit,
    onCloseTimeChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(day.dayName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(8.dp))
                    if (day.isOpen) {
                        Text(
                            "OPEN",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            "CLOSED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = day.isOpen,
                    onCheckedChange = onToggle,
                    thumbContent = {
                        if (day.isOpen) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            if (day.isOpen) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TimeCell("OPENING TIME", day.openTime ?: "09:00 AM", onOpenTimeChange, Modifier.weight(1f))
                    Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TimeCell("CLOSING TIME", day.closeTime ?: "06:00 PM", onCloseTimeChange, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TimeCell(
    label: String,
    time: String,
    onTimeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {
                val (h, m) = parseTime(time)
                TimePickerDialog(context, { _, hour, minute ->
                    onTimeChanged(formatTime(hour, minute))
                }, h, m, false).show()
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(time, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun defaultWeeklySchedule(): List<BusinessHourDTO> = listOf(
    BusinessHourDTO("Monday", 0, true, "09:00 AM", "06:00 PM"),
    BusinessHourDTO("Tuesday", 1, true, "09:00 AM", "06:00 PM"),
    BusinessHourDTO("Wednesday", 2, true, "09:00 AM", "06:00 PM"),
    BusinessHourDTO("Thursday", 3, true, "09:00 AM", "06:00 PM"),
    BusinessHourDTO("Friday", 4, true, "09:00 AM", "08:00 PM"),
    BusinessHourDTO("Saturday", 5, false, null, null),
    BusinessHourDTO("Sunday", 6, false, null, null)
)

private fun parseTime(time: String): Pair<Int, Int> {
    return try {
        val clean = time.uppercase(Locale.US).replace("AM", "").replace("PM", "").trim()
        val parts = clean.split(":")
        var hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val pm = time.uppercase(Locale.US).contains("PM")
        if (pm && hour != 12) hour += 12
        if (!pm && hour == 12) hour = 0
        hour to minute
    } catch (_: Exception) {
        9 to 0
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val normalizedHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.US, "%02d:%02d %s", normalizedHour, minute, amPm)
}
