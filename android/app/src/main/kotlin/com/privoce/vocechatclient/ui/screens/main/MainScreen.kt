package com.privoce.vocechatclient.ui.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.data.models.UserInfo
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import com.privoce.vocechatclient.ui.components.ConnectionBadge
import com.privoce.vocechatclient.ui.screens.dialogs.CreateChannelDialog
import com.privoce.vocechatclient.ui.screens.dialogs.UserDetailDialog
import com.privoce.vocechatclient.ui.screens.main.tabs.ChatsTab
import com.privoce.vocechatclient.ui.screens.main.tabs.ContactsTab
import com.privoce.vocechatclient.ui.screens.main.tabs.SettingsTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: VoceChatRepository,
    onOpenChat: (chatType: String, id: Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Chats, 1: Contacts, 2: Settings
    var showCreateChannelDialog by remember { mutableStateOf(false) }
    var selectedUserDetail by remember { mutableStateOf<UserInfo?>(null) }

    val connectionStatus by repository.connectionStatus.collectAsState()
    val serverInfo by repository.serverInfo.collectAsState()

    if (showCreateChannelDialog) {
        CreateChannelDialog(
            repository = repository,
            onDismiss = { showCreateChannelDialog = false },
            onCreated = { gid ->
                showCreateChannelDialog = false
                onOpenChat("channel", gid)
            }
        )
    }

    selectedUserDetail?.let { user ->
        UserDetailDialog(
            user = user,
            repository = repository,
            onDismiss = { selectedUserDetail = null },
            onStartChat = { uid ->
                selectedUserDetail = null
                onOpenChat("dm", uid)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = when (selectedTab) {
                                0 -> serverInfo?.name ?: stringResource(R.string.app_name)
                                1 -> stringResource(R.string.tab_contacts)
                                else -> stringResource(R.string.tab_settings)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        ConnectionBadge(
                            status = connectionStatus,
                            onRetry = { repository.initSse() }
                        )
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { repository.loadInitialData() }) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_refresh))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Default.ChatBubble else Icons.Default.ChatBubbleOutline,
                            contentDescription = stringResource(R.string.tab_chats)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_chats)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            if (selectedTab == 1) Icons.Default.People else Icons.Default.PeopleOutline,
                            contentDescription = stringResource(R.string.tab_contacts)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_contacts)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            if (selectedTab == 2) Icons.Default.Settings else Icons.Default.SettingsSuggest,
                            contentDescription = stringResource(R.string.tab_settings)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_settings)) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showCreateChannelDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_channel_title))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Crossfade(targetState = selectedTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    0 -> ChatsTab(
                        repository = repository,
                        onOpenChat = onOpenChat
                    )
                    1 -> ContactsTab(
                        repository = repository,
                        onUserClick = { user -> selectedUserDetail = user }
                    )
                    2 -> SettingsTab(
                        repository = repository,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

