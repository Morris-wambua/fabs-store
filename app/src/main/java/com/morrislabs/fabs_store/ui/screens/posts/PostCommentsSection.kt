package com.morrislabs.fabs_store.ui.screens.posts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morrislabs.fabs_store.data.model.CommentDTO
import com.morrislabs.fabs_store.util.formatTimeAgo

@Composable
fun PostCommentsSection(
    comments: List<CommentDTO>,
    onAddComment: (String) -> Unit,
    onReply: (commentId: String, content: String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onToggleCommentLike: (String) -> Unit,
    onEditComment: (commentId: String, content: String) -> Unit,
    onLoadMore: () -> Unit,
    hasMore: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CommentInput(onAddComment = onAddComment)

        Spacer(modifier = Modifier.height(8.dp))

        if (comments.isEmpty()) {
            EmptyCommentsState()
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                comments.forEach { comment ->
                    CommentItem(
                        comment = comment,
                        onReply = onReply,
                        onDeleteComment = onDeleteComment,
                        onToggleCommentLike = onToggleCommentLike,
                        onEditComment = onEditComment,
                        isLastItem = comment == comments.lastOrNull()
                    )
                }

                if (hasMore) {
                    TextButton(
                        onClick = onLoadMore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Load more comments",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentInput(onAddComment: (String) -> Unit) {
    var commentText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            placeholder = {
                Text(
                    text = "Add a comment...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (commentText.isNotBlank()) {
                    onAddComment(commentText.trim())
                    commentText = ""
                }
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send comment",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyCommentsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No comments yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Be the first to share your thoughts!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CommentItem(
    comment: CommentDTO,
    onReply: (commentId: String, content: String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onToggleCommentLike: (String) -> Unit,
    onEditComment: (commentId: String, content: String) -> Unit,
    isLastItem: Boolean = false
) {
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(comment.content) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CommentAvatar(
                initial = comment.userFirstName?.firstOrNull()?.toString() ?: "U",
                backgroundColor = Color(
                    comment.userFirstName?.hashCode()?.toLong()?.rem(0xFFFFFF)?.toInt()
                        ?: 0xFF9E9E9E.toInt()
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${comment.userFirstName ?: ""} ${comment.userLastName ?: ""}".trim()
                        .ifEmpty { "Unknown User" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                comment.dateCreated?.let {
                    Text(
                        text = " · ${formatTimeAgo(it)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (comment.edited) {
                    Text(
                        text = " · edited",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            IconButton(
                onClick = { isEditing = !isEditing },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit comment",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            IconButton(
                onClick = { onDeleteComment(comment.id ?: "") },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete comment",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isEditing) {
            OutlinedTextField(
                value = editedText,
                onValueChange = { editedText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                maxLines = 4
            )
            Row(modifier = Modifier.padding(start = 44.dp, top = 4.dp)) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        if (editedText.isNotBlank()) {
                            onEditComment(comment.id ?: "", editedText.trim())
                            isEditing = false
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        editedText = comment.content
                        isEditing = false
                    }
                )
            }
        } else {
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 44.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.padding(start = 44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { onToggleCommentLike(comment.id ?: "") }
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Like comment",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${comment.likeCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Text(
                text = "Reply",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { showReplyField = !showReplyField }
            )
        }

        if (showReplyField) {
            ReplyInput(
                replyText = replyText,
                onReplyTextChange = { replyText = it },
                onSend = {
                    if (replyText.isNotBlank()) {
                        onReply(comment.id ?: "", replyText.trim())
                        replyText = ""
                        showReplyField = false
                    }
                }
            )
        }

        if (comment.replies.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 44.dp, top = 8.dp)) {
                comment.replies.forEach { reply ->
                    ReplyItem(reply = reply)
                }
            }
        }

        if (!isLastItem) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun ReplyInput(
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 44.dp, top = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = replyText,
            onValueChange = onReplyTextChange,
            placeholder = {
                Text(
                    text = "Write a reply...",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true
        )

        IconButton(onClick = onSend) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send reply",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ReplyItem(reply: CommentDTO) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        CommentAvatar(
            initial = reply.userFirstName?.firstOrNull()?.toString() ?: "U",
            backgroundColor = Color(
                reply.userFirstName?.hashCode()?.toLong()?.rem(0xFFFFFF)?.toInt()
                    ?: 0xFF9E9E9E.toInt()
            ),
            size = 28
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${reply.userFirstName ?: ""} ${reply.userLastName ?: ""}".trim()
                        .ifEmpty { "Unknown User" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                reply.dateCreated?.let {
                    Text(
                        text = " · ${formatTimeAgo(it)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = reply.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun CommentAvatar(
    initial: String,
    backgroundColor: Color,
    size: Int = 34
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size / 2.6).sp
        )
    }
}

