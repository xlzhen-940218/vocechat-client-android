package com.privoce.vocechatclient.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privoce.vocechatclient.data.repository.ConnectionStatus
import com.privoce.vocechatclient.ui.theme.OnlineGreen

@Composable
fun ConnectionBadge(
    status: ConnectionStatus,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = status != ConnectionStatus.CONNECTED,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val (bgColor, textColor, dotColor, text) = when (status) {
            ConnectionStatus.CONNECTING -> Quadruple(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
                MaterialTheme.colorScheme.primary,
                "正在连接..."
            )
            ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> Quadruple(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
                MaterialTheme.colorScheme.error,
                "未连接 (点击重试)"
            )
            ConnectionStatus.CONNECTED -> Quadruple(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                OnlineGreen,
                "实时连接"
            )
        }

        Row(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .clickable { if (status != ConnectionStatus.CONNECTING) onRetry() }
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

