package com.privoce.vocechatclient.ui.screens.main.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import com.privoce.vocechatclient.ui.components.VoceAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    repository: VoceChatRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val serverInfo by repository.serverInfo.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    val themeMode by repository.preferences.themeModeFlow.collectAsState()
    val dynamicColorEnabled by repository.preferences.dynamicColorFlow.collectAsState()

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.settings_logout_title)) },
            text = { Text(stringResource(R.string.settings_logout_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        repository.logout()
                        onLogout()
                    }
                ) {
                    Text(stringResource(R.string.settings_logout_button), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoceAvatar(
                    avatarUrl = currentUser?.let { repository.apiClient.getUserAvatarUrl(it.uid, it.avatarUpdatedAt) },
                    name = currentUser?.displayName ?: stringResource(R.string.app_name),
                    size = 64.dp,
                    isBot = currentUser?.isBot == true
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = currentUser?.displayName ?: stringResource(R.string.settings_not_signed_in),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentUser?.email ?: currentUser?.uid?.let {
                            stringResource(R.string.fallback_uid, it)
                        } ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (currentUser?.isAdmin == true) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.user_system_admin),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Server Information Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.settings_server_info),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingsItemRow(
                    icon = Icons.Default.Dns,
                    title = stringResource(R.string.settings_server_address),
                    subtitle = repository.preferences.serverUrl
                )
                SettingsItemRow(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_server_name),
                    subtitle = serverInfo?.name ?: stringResource(R.string.app_name)
                )
                SettingsItemRow(
                    icon = Icons.Default.CloudQueue,
                    title = stringResource(R.string.settings_version),
                    subtitle = serverInfo?.version ?: "-"
                )
            }
        }

        // Appearance Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.settings_appearance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                val themeOptions = listOf(
                    "system" to stringResource(R.string.settings_theme_system),
                    "light" to stringResource(R.string.settings_theme_light),
                    "dark" to stringResource(R.string.settings_theme_dark)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    themeOptions.forEachIndexed { index, (value, label) ->
                        SegmentedButton(
                            selected = themeMode == value,
                            onClick = { repository.preferences.themeMode = value },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = themeOptions.size
                            )
                        ) {
                            Text(label, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_dynamic_color),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.settings_dynamic_color_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dynamicColorEnabled,
                        onCheckedChange = {
                            repository.preferences.dynamicColor = it
                        }
                    )
                }
            }
        }

        // About & Logout
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SettingsItemRow(
                    icon = Icons.Default.Android,
                    title = stringResource(R.string.settings_architecture),
                    subtitle = stringResource(R.string.settings_architecture_desc)
                )
                SettingsItemRow(
                    icon = Icons.Default.Security,
                    title = stringResource(R.string.settings_privacy),
                    subtitle = stringResource(R.string.settings_privacy_desc)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_logout_button))
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

