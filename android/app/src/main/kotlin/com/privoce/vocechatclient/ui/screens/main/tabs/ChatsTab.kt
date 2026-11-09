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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.data.models.GroupInfo
import com.privoce.vocechatclient.data.models.UserInfo
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import com.privoce.vocechatclient.ui.components.EmptyStateView
import com.privoce.vocechatclient.ui.components.VoceAvatar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val unreadCounts by repository.unreadCounts.collectAsState()
    val currentUserId = repository.preferences.currentUserId
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }

    val filteredChannels = remember(channels, searchQuery, messagesMap) {
        val base = if (searchQuery.isBlank()) channels
        else channels.filter { it.name.contains(searchQuery, ignoreCase = true) }
        base.sortedByDescending {
            messagesMap["channel_${it.gid}"]?.lastOrNull()?.createdAt ?: 0L
        }
    }

    val directMessageUsers = remember(users, currentUserId, searchQuery, messagesMap) {
        val base = if (searchQuery.isBlank()) {
            // Only show users we already have a conversation with.
            users.filter {
                it.uid != currentUserId &&
                    !messagesMap["dm_${it.uid}"].isNullOrEmpty()
            }
        } else {
            users.filter {
                it.uid != currentUserId &&
                    (it.name.contains(searchQuery, ignoreCase = true) ||
                        it.email?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
        base.sortedByDescending {
            messagesMap["dm_${it.uid}"]?.lastOrNull()?.createdAt ?: 0L
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.chats_search_placeholder), fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear))
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
                title = stringResource(R.string.chats_empty_title),
                description = stringResource(R.string.chats_empty_desc),
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
                            text = stringResource(R.string.chats_section_channels, filteredChannels.size),
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
                            lastMessage = lastMsg?.previewText(context),
                            lastMessageTime = lastMsg?.let { formatChatTime(it.createdAt) },
                            unreadCount = unreadCounts["channel_${channel.gid}"] ?: 0,
                            onClick = { onOpenChat("channel", channel.gid) }
                        )
                    }
                }

                // Direct Messages Section
                if (directMessageUsers.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.chats_section_dm),
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
                            lastMessage = lastMsg?.previewText(context),
                            lastMessageTime = lastMsg?.let { formatChatTime(it.createdAt) },
                            unreadCount = unreadCounts["dm_${user.uid}"] ?: 0,
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
    lastMessageTime: String?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                    }
                }
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
                    text = lastMessage ?: channel.description ?: stringResource(R.string.chats_channel_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (lastMessageTime != null) {
                    Text(
                        text = lastMessageTime,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val memberCount = channel.members?.size ?: 0
                if (memberCount > 0) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = memberCount.toString(),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
    lastMessageTime: String?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                    }
                }
            ) {
                VoceAvatar(
                    avatarUrl = avatarUrl,
                    name = user.displayName,
                    size = 44.dp,
                    showOnlineBadge = true,
                    isOnline = user.online,
                    isBot = user.isBot
                )
            }

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
                                text = stringResource(R.string.action_admin),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = lastMessage ?: stringResource(
                        if (user.online) R.string.chats_online_now else R.string.chats_offline
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (lastMessageTime != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = lastMessageTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatChatTime(createdAt: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = createdAt }
    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    val pattern = when {
        sameDay -> "HH:mm"
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> "MM/dd"
        else -> "yyyy/MM/dd"
    }
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(createdAt))
}

