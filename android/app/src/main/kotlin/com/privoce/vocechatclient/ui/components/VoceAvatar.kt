package com.privoce.vocechatclient.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.ui.theme.OfflineGray
import com.privoce.vocechatclient.ui.theme.OnlineGreen

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
    val defaultRes = if (isBot) R.drawable.bot else R.drawable.default_avatar

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .placeholder(defaultRes)
                    .error(defaultRes)
                    .fallback(defaultRes)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            // Default avatar resource
            Image(
                painter = painterResource(id = defaultRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
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
