package com.morrislabs.fabs_store.ui.screens.posts.createflow

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.morrislabs.fabs_store.data.model.PostType
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ToolBtnBg = Color(0x33000000)
private val SpeedPopupBg = Color(0x99000000)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordVideoScreen(
    viewModel: CreatePostFlowViewModel,
    onClose: () -> Unit,
    onNavigateToTrimCrop: () -> Unit,
    onNavigateToSounds: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val timerCountdownSec by viewModel.timerCountdown.collectAsState()
    val timerStopAtSec by viewModel.timerStopAtSeconds.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val permissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    )
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(LifecycleCameraController.VIDEO_CAPTURE)
        }
    }
    var isRecording by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var countdownDisplay by remember { mutableIntStateOf(0) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }
    var autoStopJob by remember { mutableStateOf<Job?>(null) }
    var showSpeedPopup by remember { mutableStateOf(false) }
    var showTimerSheet by remember { mutableStateOf(false) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.setMediaUri(it, PostType.VIDEO); onNavigateToTrimCrop() }
    }

    DisposableEffect(draft.useFrontCamera) {
        cameraController.cameraSelector = if (draft.useFrontCamera)
            CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        onDispose { }
    }
    DisposableEffect(draft.flashEnabled) {
        cameraController.enableTorch(draft.flashEnabled)
        onDispose { }
    }
    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose {
            countdownJob?.cancel()
            autoStopJob?.cancel()
            activeRecording?.stop()
            cameraController.unbind()
        }
    }

    if (!permissions.allPermissionsGranted) {
        PermissionRequest { permissions.launchMultiplePermissionRequest() }
        return
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        TopSection(draft, onClose, onNavigateToSounds)

        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ToolRailBtn(Icons.Default.FlipCameraAndroid, "FLIP") { viewModel.toggleCamera() }
            Box {
                ToolRailBtn(
                    Icons.Default.Speed, "SPEED",
                    tint = if (showSpeedPopup) RecordGreen else Color.White
                ) { showSpeedPopup = !showSpeedPopup }
                if (showSpeedPopup) {
                    SpeedPopup(
                        draft.recordingSpeed, { viewModel.setRecordingSpeed(it) },
                        Modifier.align(Alignment.CenterStart).offset(x = (-56).dp)
                    )
                }
            }
            ToolRailBtn(Icons.Default.FilterList, "FILTERS") { }
            ToolRailBtn(Icons.Default.Timer, "TIMER") { showTimerSheet = true }
            ToolRailBtn(
                if (draft.flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff, "FLASH"
            ) { viewModel.toggleFlash() }
        }

        BottomSection(
            draft = draft,
            isRecording = isRecording,
            onRecord = {
                if (isRecording) {
                    countdownJob?.cancel()
                    autoStopJob?.cancel()
                    activeRecording?.stop()
                    activeRecording = null
                    isRecording = false
                } else {
                    val startRecordingNow = {
                        @Suppress("MissingPermission")
                        val file = File(context.cacheDir, "video_${System.currentTimeMillis()}.mp4")
                        val opts = FileOutputOptions.Builder(file).build()
                        val audioConfig = AudioConfig.create(true)
                        activeRecording = cameraController.startRecording(
                            opts, audioConfig, ContextCompat.getMainExecutor(context)
                        ) { event ->
                            if (event is VideoRecordEvent.Finalize) {
                                isRecording = false
                                activeRecording = null
                                autoStopJob?.cancel()
                                if (!event.hasError()) {
                                    viewModel.setMediaUri(Uri.fromFile(file), PostType.VIDEO)
                                    onNavigateToTrimCrop()
                                }
                            }
                        }
                        isRecording = true

                        val stopAfter = timerStopAtSec
                        if (stopAfter != null && stopAfter > 0f) {
                            autoStopJob?.cancel()
                            autoStopJob = scope.launch {
                                delay((stopAfter * 1000f).toLong())
                                if (isRecording) {
                                    activeRecording?.stop()
                                }
                            }
                        }
                    }

                    val countdown = timerCountdownSec
                    if (countdown != null && countdown > 0) {
                        countdownJob?.cancel()
                        countdownJob = scope.launch {
                            for (second in countdown downTo 1) {
                                countdownDisplay = second
                                delay(1000)
                            }
                            countdownDisplay = 0
                            startRecordingNow()
                        }
                    } else {
                        startRecordingNow()
                    }
                }
            },
            onUpload = { galleryLauncher.launch("video/*") },
            onDurationChange = { viewModel.setDurationMode(it) }
        )

        if (countdownDisplay > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdownDisplay.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 52.sp
                )
            }
        }

        if (showTimerSheet) {
            TimerBottomSheet(
                initialCountdown = timerCountdownSec ?: 3,
                initialStopAt = timerStopAtSec ?: 6.5f,
                onDismiss = { showTimerSheet = false },
                onApply = { countdown, stopAt ->
                    viewModel.setTimerCountdown(countdown)
                    viewModel.setTimerStopAtSeconds(stopAt)
                    showTimerSheet = false
                }
            )
        }
    }
}

@Composable
private fun TopSection(draft: CreatePostDraft, onClose: () -> Unit, onSounds: () -> Unit) {
    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.clip(RoundedCornerShape(50))
                    .background(Color(0x4D000000))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                    .clickable { onSounds() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.MusicNote, "Sounds", tint = Color.White, modifier = Modifier.size(16.dp))
                Text("Sounds", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp)
                .clip(RoundedCornerShape(1.dp)).background(Color(0x33FFFFFF))
        ) {
            Box(Modifier.fillMaxWidth(0.33f).height(1.dp).background(RecordGreen))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(RecordGreen))
            Text(
                "RECORDING MODE", color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BottomSection(
    draft: CreatePostDraft,
    isRecording: Boolean,
    onRecord: () -> Unit,
    onUpload: () -> Unit,
    onDurationChange: (DurationMode) -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                .navigationBarsPadding().padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFEC407A), Color(0xFFFDD835))))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Face, "Effects", tint = Color.White, modifier = Modifier.size(24.dp)) }

                Box(
                    Modifier.size(80.dp).border(4.dp, Color.White, CircleShape).padding(7.dp)
                        .clip(CircleShape).background(if (isRecording) Color.Red else RecordGreen)
                        .clickable(onClick = onRecord)
                )

                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .background(Color(0x33FFFFFF)).clickable(onClick = onUpload),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.PhotoLibrary, "Upload", tint = Color.White, modifier = Modifier.size(24.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                DurationChip("60s", draft.durationMode == DurationMode.S60) { onDurationChange(DurationMode.S60) }
                DurationChip("15s", draft.durationMode == DurationMode.S15) { onDurationChange(DurationMode.S15) }
                DurationChip("Templates", draft.durationMode == DurationMode.TEMPLATES) { onDurationChange(DurationMode.TEMPLATES) }
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.width(32.dp).height(1.dp).clip(RoundedCornerShape(1.dp)).background(Color.White.copy(alpha = 0.4f)))
        }
    }
}

@Composable
private fun ToolRailBtn(icon: ImageVector, label: String, tint: Color = Color.White, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(ToolBtnBg).clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) { Icon(icon, label, tint = tint, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.height(4.dp))
        Text(label, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DurationChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            text,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        if (selected) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.size(4.dp).clip(CircleShape).background(Color.White))
        }
    }
}

@Composable
private fun SpeedPopup(currentSpeed: Float, onSelect: (Float) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(50)).background(SpeedPopupBg)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(0.3f, 0.5f, 1f, 2f, 3f).forEach { speed ->
            val sel = currentSpeed == speed
            Box(
                Modifier.size(40.dp)
                    .then(if (sel) Modifier.border(1.5.dp, RecordGreen, CircleShape) else Modifier)
                    .clip(CircleShape).clickable { onSelect(speed) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${speed}x",
                    color = if (sel) RecordGreen else Color.White,
                    fontSize = 12.sp,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}


