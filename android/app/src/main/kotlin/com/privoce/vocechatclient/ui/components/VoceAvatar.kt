package com.privoce.vocechatclient.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.ui.theme.OfflineGray
import com.privoce.vocechatclient.ui.theme.OnlineGreen

private val avatarColors = listOf(
    Color(0xFF7E57C2), // Purple (like official Admin avatar)
    Color(0xFF26A69A), // Teal
    Color(0xFF42A5F5), // Blue
    Color(0xFFFFA726), // Orange
    Color(0xFFAB47BC), // Magenta
    Color(0xFF66BB6A)  // Green
)

@Composable
fun VoceAvatar(
    avatarUrl: String?,
    name: String,
    size: Dp = 44.dp,
    showOnlineBadge: Boolean = false,
    isOnline: Boolean = false,
    isBot: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isImageError by remember(avatarUrl) { mutableStateOf(false) }
    val initial = remember(name) {
        name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }
    val bgColor = remember(name) {
        // floorMod 可安全处理 Int.MIN_VALUE，避免取模结果为负导致越界
        avatarColors[Math.floorMod(name.hashCode(), avatarColors.size)]
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank() && !isImageError) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .listener(
                        onError = { _, _ -> isImageError = true },
                        onSuccess = { _, _ -> isImageError = false }
                    )
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            if (isBot) {
                Image(
                    painter = painterResource(id = R.drawable.bot),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.45f).sp
                    )
                }
            }
        }

        if (showOnlineBadge) {
            val badgeSize = (size.value * 0.28f).coerceIn(8f, 14f).dp
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(if (isOnline) OnlineGreen else OfflineGray)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
