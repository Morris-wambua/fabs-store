package com.morrislabs.fabs_store.ui.screens.storeonboarding

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morrislabs.fabs_store.ui.viewmodel.CreateStoreWizardViewModel
import com.morrislabs.fabs_store.ui.viewmodel.DaySchedule
import java.util.Locale

@Composable
fun BusinessHoursStepScreen(
    wizardViewModel: CreateStoreWizardViewModel,
    onNavigateBack: () -> Unit,
    onStoreCreated: () -> Unit
) {
    val state by wizardViewModel.state.collectAsState()

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) onStoreCreated()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BusinessHoursHeader(onNavigateBack = onNavigateBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "When are you open?",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Set your weekly schedule. Customers will see these times on your store profile.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                CopyMondayButton(onClick = { wizardViewModel.copyMondayToAllDays() })

                state.businessHours.forEachIndexed { index, day ->
                    DayScheduleCard(
                        day = day,
                        onToggle = { wizardViewModel.toggleDayOpen(day.dayIndex) },
                        onOpenTimeChanged = { wizardViewModel.updateOpenTime(day.dayIndex, it) },
                        onCloseTimeChanged = { wizardViewModel.updateCloseTime(day.dayIndex, it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        FinishSetupFooter(
            isSubmitting = state.isSubmitting,
            submitError = state.submitError,
            onFinish = { if (wizardViewModel.canSubmit()) wizardViewModel.submitStore() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun BusinessHoursHeader(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Business Hours",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Text(
            text = "STEP 3 OF 3",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.width(120.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CopyMondayButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Copy Monday to all days", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun DayScheduleCard(
    day: DaySchedule,
    onToggle: () -> Unit,
    onOpenTimeChanged: (String) -> Unit,
    onCloseTimeChanged: (String) -> Unit
) {
    val borderColor = if (day.isOpen) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    val cardAlpha = if (day.isOpen) 1f else 0.6f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = day.dayName,
                        color = if (day.isOpen) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (!day.isOpen) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CLOSED",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Switch(
                    checked = day.isOpen,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            if (day.isOpen) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeBox(
                        label = "OPENS AT",
                        time = day.openTime,
                        enabled = true,
                        onTimePicked = onOpenTimeChanged,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "—", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                    TimeBox(
                        label = "CLOSES AT",
                        time = day.closeTime,
                        enabled = true,
                        onTimePicked = onCloseTimeChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeBox(
                        label = "OPENS AT",
                        time = "--:--",
                        enabled = false,
                        onTimePicked = {},
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "—", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 16.sp)
                    TimeBox(
                        label = "CLOSES AT",
                        time = "--:--",
                        enabled = false,
                        onTimePicked = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeBox(
    label: String,
    time: String,
    enabled: Boolean,
    onTimePicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textColor = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val iconColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .then(
                if (enabled) {
                    Modifier.clickable {
                        val (hour, minute) = parseTime(time)
                        TimePickerDialog(context, { _, h, m ->
                            onTimePicked(formatTime(h, m))
                        }, hour, minute, false).show()
                    }
                } else Modifier
            )
            .padding(12.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = time, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun FinishSetupFooter(
    isSubmitting: Boolean,
    submitError: String?,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                    startY = 0f,
                    endY = 60f
                )
            )
            .padding(top = 24.dp)
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        if (submitError != null) {
            Text(
                text = submitError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isSubmitting,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Finish Setup",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

private fun parseTime(time: String): Pair<Int, Int> {
    return try {
        val parts = time.uppercase(Locale.US).replace("AM", "").replace("PM", "").trim().split(":")
        var hour = parts[0].trim().toInt()
        val minute = parts.getOrNull(1)?.trim()?.toInt() ?: 0
        val isPm = time.uppercase(Locale.US).contains("PM")
        if (isPm && hour != 12) hour += 12
        if (!isPm && hour == 12) hour = 0
        hour to minute
    } catch (_: Exception) {
        9 to 0
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
}
