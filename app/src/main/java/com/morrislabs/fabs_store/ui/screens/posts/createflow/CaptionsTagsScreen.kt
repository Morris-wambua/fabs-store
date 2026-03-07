package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.morrislabs.fabs_store.data.model.HashtagSuggestionDTO

@Composable
fun CaptionsTagsScreen(
    draft: CreatePostDraft,
    suggestions: List<HashtagSuggestionDTO>,
    showSuggestions: Boolean,
    onBack: () -> Unit,
    onSaveDraft: () -> Unit,
    onCaptionChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onPublish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF102216))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Finalize Post Details",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "Save Draft",
                color = Color(0xFF0CED5B),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSaveDraft)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            VideoSummaryCard(draft)

            Text(
                text = "Caption",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )

            Box {
                if (showSuggestions && suggestions.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = "SUGGESTIONS",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                        suggestions.take(3).forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSuggestionClick(item.hashtag) }
                                    .background(if (index == 0) Color(0x330CED5B) else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Tag, contentDescription = null, tint = if (index == 0) Color(0xFF0CED5B) else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                Text(
                                    text = item.hashtag,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                                )
                                Text(
                                    text = "${item.usageCount}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            TextField(
                value = draft.caption,
                onValueChange = onCaptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .padding(top = 8.dp),
                placeholder = {
                    Text("Getting ready for the weekend ✨\n#hai", color = Color(0xFF94A3B8))
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x1A0CED5B),
                    unfocusedContainerColor = Color(0x1A0CED5B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0x550CED5B),
                    unfocusedIndicatorColor = Color(0x330CED5B),
                    cursorColor = Color(0xFF0CED5B)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            PostOptionRow(Icons.Default.LocationOn, "Add Location")
            PostOptionRow(Icons.Default.PersonAdd, "Tag People")
            PostOptionRow(Icons.Default.Settings, "Advanced Post Settings")

            Box(modifier = Modifier.height(120.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xEE102216))
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Button(
                onClick = onPublish,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0CED5B), contentColor = Color(0xFF102216)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Preview Post", fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun VideoSummaryCard(draft: CreatePostDraft) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1A0CED5B))
            .border(1.dp, Color(0x330CED5B), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = draft.mediaUri,
                contentDescription = "Video preview",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
            )
            Icon(
                Icons.Default.PlayCircleFilled,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
            )
        }

        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                text = draft.mediaUri?.lastPathSegment ?: "weekend_vibes_final.mp4",
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            val durationSec = (draft.trim.endMs - draft.trim.startMs) / 1000
            val mins = durationSec / 60
            val secs = durationSec % 60
            Text(
                text = "%d:%02d • 1080p High Quality".format(mins, secs),
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
    }
}

@Composable
private fun PostOptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x1A0CED5B))
            .border(1.dp, Color(0x220CED5B), RoundedCornerShape(10.dp))
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF0CED5B))
        Text(text = label, color = Color.White, modifier = Modifier.weight(1f).padding(start = 10.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
    }
}
