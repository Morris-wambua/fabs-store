package com.morrislabs.fabs_store.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.data.model.FetchStoreResponse
import com.morrislabs.fabs_store.data.model.PostDTO
import com.morrislabs.fabs_store.ui.viewmodel.PostViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

data class ChecklistItem(
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean,
    val action: (() -> Unit)? = null
)

@Composable
fun SetupChecklistScreen(
    onNavigateBack: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToCreateExpert: (String) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    storeViewModel: StoreViewModel = viewModel(),
    postViewModel: PostViewModel = viewModel()
) {
    val storeState by storeViewModel.storeState.collectAsState()
    val postsState by postViewModel.postsState.collectAsState()

    LaunchedEffect(Unit) {
        if (storeState !is StoreViewModel.StoreState.Success) {
            storeViewModel.fetchUserStore()
        }
    }

    val store = (storeState as? StoreViewModel.StoreState.Success)?.data

    LaunchedEffect(store?.id) {
        val storeId = store?.id
        if (!storeId.isNullOrEmpty()) {
            postViewModel.fetchStorePosts(storeId)
        }
    }

    val hasPosts = when (postsState) {
        is StoreViewModel.LoadingState.Success ->
            (postsState as StoreViewModel.LoadingState.Success<List<PostDTO>>).data.isNotEmpty()
        else -> false
    }

    val items = buildChecklistItems(
        store = store,
        hasPosts = hasPosts,
        onAddService = onNavigateToServices,
        onOnboardExpert = { store?.id?.let { onNavigateToCreateExpert(it) } },
        onCreatePost = onNavigateToCreatePost
    )

    val completedCount = items.count { it.isCompleted }
    val totalCount = items.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val percentComplete = (progress * 100).toInt()
    val storeName = store?.name ?: "Your Store"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ChecklistTopBar(onNavigateBack = onNavigateBack)

            HeroCelebrationSection(storeName = storeName)

            ProgressCard(
                percentComplete = percentComplete,
                completedCount = completedCount,
                totalCount = totalCount,
                progress = progress
            )

            Spacer(modifier = Modifier.height(24.dp))

            val remaining = items.filter { !it.isCompleted }
            val completed = items.filter { it.isCompleted }

            if (remaining.isNotEmpty()) {
                RemainingTasksSection(tasks = remaining)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (completed.isNotEmpty()) {
                CompletedTasksSection(tasks = completed)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PromoHintBox()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun buildChecklistItems(
    store: FetchStoreResponse?,
    hasPosts: Boolean,
    onAddService: () -> Unit,
    onOnboardExpert: () -> Unit,
    onCreatePost: () -> Unit
): List<ChecklistItem> {
    val profileComplete = store != null
    val hasServices = !store?.servicesOffered.isNullOrEmpty()
    val hasExperts = (store?.noOfExperts ?: 0) > 0
    val hasPost = hasPosts

    return listOf(
        ChecklistItem(
            title = "Complete store profile",
            subtitle = "Basic info and branding",
            isCompleted = profileComplete
        ),
        ChecklistItem(
            title = "Add your first service",
            subtitle = "Show clients what you offer",
            isCompleted = hasServices,
            action = onAddService
        ),
        ChecklistItem(
            title = "Onboard an expert",
            subtitle = "Invite team members to help",
            isCompleted = hasExperts,
            action = onOnboardExpert
        ),
        ChecklistItem(
            title = "Create your first post",
            subtitle = "Engagement starts here",
            isCompleted = hasPost,
            action = onCreatePost
        )
    )
}

@Composable
private fun ChecklistTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
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
            text = "Setup Checklist",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun HeroCelebrationSection(storeName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString {
                append("You're almost there, ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("$storeName!")
                }
            },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Complete these steps to launch your business today.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun ProgressCard(
    percentComplete: Int,
    completedCount: Int,
    totalCount: Int,
    progress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "OVERALL PROGRESS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$percentComplete% Complete",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$completedCount of $totalCount tasks",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun RemainingTasksSection(tasks: List<ChecklistItem>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "REMAINING TASKS",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        tasks.forEach { task ->
            RemainingTaskCard(task = task)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RemainingTaskCard(task: ChecklistItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = task.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { task.action?.invoke() },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text(
                    text = "Go",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CompletedTasksSection(tasks: List<ChecklistItem>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "COMPLETED",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        tasks.forEach { task ->
            CompletedTaskCard(task = task)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CompletedTaskCard(task: ChecklistItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.75f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textDecoration = TextDecoration.LineThrough
                )
                Text(
                    text = task.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PromoHintBox() {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val borderColor = primaryColor.copy(alpha = 0.4f)
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            drawRoundRect(
                color = borderColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(primaryColor.copy(alpha = 0.05f))
                .padding(24.dp)
        ) {
            Text(
                text = "\"Stores that complete their setup are 3x more likely to get their first booking within 24 hours.\"",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
