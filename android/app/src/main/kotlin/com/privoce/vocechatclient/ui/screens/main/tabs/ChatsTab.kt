package com.privoce.vocechatclient.ui.screens.main.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privoce.vocechatclient.data.models.GroupInfo
import com.privoce.vocechatclient.data.models.UserInfo
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import com.privoce.vocechatclient.ui.components.EmptyStateView
import com.privoce.vocechatclient.ui.components.VoceAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTab(
    repository: VoceChatRepository,
    onOpenChat: (chatType: String, id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val channels by repository.channels.collectAsState()
    val users by repository.users.collectAsState()
    val messagesMap by repository.messages.collectAsState()
    val currentUserId = repository.preferences.currentUserId

    var searchQuery by remember { mutableStateOf("") }

    val filteredChannels = remember(channels, searchQuery) {
        if (searchQuery.isBlank()) channels
        else channels.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val directMessageUsers = remember(users, currentUserId, searchQuery) {
        val list = users.filter { it.uid != currentUserId }
        if (searchQuery.isBlank()) list
        else list.filter { it.name.contains(searchQuery, ignoreCase = true) || it.email?.contains(searchQuery, ignoreCase = true) == true }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("搜索频道或私聊...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (filteredChannels.isEmpty() && directMessageUsers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.ChatBubbleOutline,
                title = "暂无消息",
                description = "点击右下角按钮创建新频道或发起对话",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Channels Section
                if (filteredChannels.isNotEmpty()) {
                    item {
                        Text(
                            text = "频道 (${filteredChannels.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                        )
                    }

                    items(filteredChannels, key = { "chan_${it.gid}" }) { channel ->
                        val lastMsg = messagesMap["channel_${channel.gid}"]?.lastOrNull()

                        ChannelItem(
                            channel = channel,
                            lastMessage = lastMsg?.previewText,
                            onClick = { onOpenChat("channel", channel.gid) }
                        )
                    }
                }

                // Direct Messages Section
                if (directMessageUsers.isNotEmpty()) {
                    item {
                        Text(
                            text = "私信消息",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                        )
                    }

                    items(directMessageUsers, key = { "dm_${it.uid}" }) { user ->
                        val lastMsg = messagesMap["dm_${user.uid}"]?.lastOrNull()

                        DirectMessageItem(
                            user = user,
                            avatarUrl = repository.apiClient.getUserAvatarUrl(user.uid, user.avatarUpdatedAt),
                            lastMessage = lastMsg?.previewText,
                            onClick = { onOpenChat("dm", user.uid) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ChannelItem(
    channel: GroupInfo,
    lastMessage: String?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (channel.isPublic) Icons.Default.Tag else Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = lastMessage ?: channel.description ?: "点击进入频道聊天",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            channel.members?.size?.let { count ->
                if (count > 0) {
                    SuggestionChip(
                        onClick = onClick,
                        label = { Text("$count 人", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectMessageItem(
    user: UserInfo,
    avatarUrl: String?,
    lastMessage: String?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VoceAvatar(
                avatarUrl = avatarUrl,
                name = user.displayName,
                size = 44.dp,
                showOnlineBadge = true,
                isOnline = user.online,
                isBot = user.isBot
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.isAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "管理员",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = lastMessage ?: if (user.online) "当前在线" else "离线",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

