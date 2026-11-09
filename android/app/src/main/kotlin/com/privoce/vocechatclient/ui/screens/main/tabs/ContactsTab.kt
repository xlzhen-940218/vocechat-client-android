package com.privoce.vocechatclient.ui.screens.main.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.data.models.UserInfo
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import com.privoce.vocechatclient.ui.components.EmptyStateView
import com.privoce.vocechatclient.ui.components.VoceAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsTab(
    repository: VoceChatRepository,
    onUserClick: (UserInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val users by repository.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var onlineOnly by remember { mutableStateOf(false) }

    val currentUserId = repository.preferences.currentUserId
    val visibleUsers = remember(users, currentUserId) {
        users.filter { it.uid != currentUserId }
    }

    val filteredUsers = remember(visibleUsers, searchQuery, onlineOnly) {
        visibleUsers.filter { user ->
            val matchQuery = searchQuery.isBlank() ||
                    user.displayName.contains(searchQuery, ignoreCase = true) ||
                    user.email?.contains(searchQuery, ignoreCase = true) == true
            val matchOnline = !onlineOnly || user.online
            matchQuery && matchOnline
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search & Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.contacts_search_placeholder), fontSize = 14.sp) },
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !onlineOnly,
                onClick = { onlineOnly = false },
                label = { Text(stringResource(R.string.contacts_filter_all, visibleUsers.size)) }
            )
            FilterChip(
                selected = onlineOnly,
                onClick = { onlineOnly = true },
                label = { Text(stringResource(R.string.contacts_filter_online, visibleUsers.count { it.online })) }
            )
        }

        if (filteredUsers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.PeopleOutline,
                title = stringResource(R.string.contacts_empty_title),
                description = stringResource(
                    if (searchQuery.isNotEmpty()) R.string.contacts_empty_search_hint
                    else R.string.contacts_empty_no_members
                ),
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredUsers, key = { it.uid }) { user ->
                    Surface(
                        onClick = { onUserClick(user) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VoceAvatar(
                                avatarUrl = repository.apiClient.getUserAvatarUrl(user.uid, user.avatarUpdatedAt),
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
                                                text = stringResource(R.string.action_admin),
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = user.email ?: stringResource(R.string.fallback_uid, user.uid),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

