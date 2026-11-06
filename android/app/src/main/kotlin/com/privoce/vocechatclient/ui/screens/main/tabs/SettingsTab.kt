package com.privoce.vocechatclient.ui.screens.main.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var currentThemeMode by remember { mutableStateOf(repository.preferences.themeMode) }
    var dynamicColorEnabled by remember { mutableStateOf(repository.preferences.dynamicColor) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出登录？") },
            text = { Text("退出后将清除本地会话，需要重新输入凭据登录。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        repository.logout()
                        onLogout()
                    }
                ) {
                    Text("退出", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
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
                    name = currentUser?.displayName ?: "User",
                    size = 64.dp,
                    isBot = currentUser?.isBot == true
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = currentUser?.displayName ?: "未登录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentUser?.email ?: "UID: ${currentUser?.uid ?: ""}",
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
                                text = "系统管理员",
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
                    text = "服务器信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingsItemRow(
                    icon = Icons.Default.Dns,
                    title = "服务器地址",
                    subtitle = repository.preferences.serverUrl
                )
                SettingsItemRow(
                    icon = Icons.Default.Info,
                    title = "服务器名称",
                    subtitle = serverInfo?.name ?: "VoceChat Server"
                )
                SettingsItemRow(
                    icon = Icons.Default.CloudQueue,
                    title = "版本",
                    subtitle = serverInfo?.version ?: "0.3.x"
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
                    text = "外观与个性化",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Material You 动态色彩",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "根据 Android 系统壁纸自动提取配色",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dynamicColorEnabled,
                        onCheckedChange = {
                            dynamicColorEnabled = it
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
                    title = "客户端架构",
                    subtitle = "Kotlin 原生 + Jetpack Compose + Material Design 3"
                )
                SettingsItemRow(
                    icon = Icons.Default.Security,
                    title = "隐私与开源",
                    subtitle = "端到端安全，无第三方遥测追踪"
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
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("退出登录")
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

