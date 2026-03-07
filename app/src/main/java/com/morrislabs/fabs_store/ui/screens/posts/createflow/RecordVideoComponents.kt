package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val RecordGreen = Color(0xFF13EC5B)
private val TimerSheetBg = Color(0xF2171717)

@Composable
internal fun TimerBottomSheet(
    initialCountdown: Int,
    initialStopAt: Float,
    onDismiss: () -> Unit,
    onApply: (Int, Float) -> Unit
) {
    var selectedCountdown by remember { mutableStateOf(initialCountdown) }
    var stopAt by remember { mutableFloatStateOf(initialStopAt) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)).clickable(onClick = onDismiss)) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(TimerSheetBg)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .clickable(enabled = false) { }
                .padding(horizontal = 24.dp).navigationBarsPadding()
        ) {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f)).align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Timer", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "COUNTDOWN", color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(0, 3, 10).forEach { s ->
                    val sel = selectedCountdown == s
                    Box(
                        Modifier.clip(RoundedCornerShape(50))
                            .then(
                                if (sel) Modifier.background(RecordGreen)
                                else Modifier.background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                            )
                            .clickable { selectedCountdown = s }
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            if (s == 0) "Off" else "${s}s",
                            color = if (sel) Color.Black else Color.White,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    "STOP RECORDING AT", color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium
                )
                Text(
                    "%.1fs".format(stopAt), color = RecordGreen,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                )
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = stopAt,
                onValueChange = { stopAt = Math.round(it * 10) / 10f },
                valueRange = 0f..60f, steps = 599,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = RecordGreen,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                listOf("0s", "15s", "30s", "60s").forEach {
                    Text(it, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(RecordGreen)
                    .clickable { onApply(selectedCountdown, stopAt) }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Timer, "Timer", tint = Color.Black, modifier = Modifier.size(20.dp))
                    Text("Start Countdown", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun PermissionRequest(onRequest: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Camera & microphone access required", color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(RecordGreen)
                    .clickable(onClick = onRequest).padding(horizontal = 32.dp, vertical = 14.dp)
            ) { Text("Grant Permissions", color = Color.Black, fontWeight = FontWeight.Bold) }
        }
    }
}
