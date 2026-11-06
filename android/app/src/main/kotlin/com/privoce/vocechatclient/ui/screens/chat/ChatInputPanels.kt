package com.privoce.vocechatclient.ui.screens.chat

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EMOJI_LIST = listOf(
    "😀", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂",
    "😉", "😍", "🥰", "😘", "😜", "🤪", "😎", "🤩", "🥳", "😏",
    "😢", "😭", "😤", "😡", "🤬", "😱", "😨", "😰", "🤔", "🤫",
    "🤗", "🤭", "😴", "😪", "🤤", "😷", "🤒", "🤕", "🥱", "😵",
    "👍", "👎", "👏", "🙌", "🤝", "🙏", "💪", "✌️", "🤞", "👌",
    "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "💔", "💯",
    "🎉", "🎊", "🔥", "✨", "⭐", "🌹", "🎁", "☕", "🍺", "🍰",
    "🚀", "💡", "✅", "❌", "⚡", "🌈", "🌙", "☀️", "😺", "🐶"
)

@Composable
fun EmojiPanel(
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(44.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 8.dp)
    ) {
        items(EMOJI_LIST) { emoji ->
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun AttachmentPanel(
    onPickImages: () -> Unit,
    onPickVideo: () -> Unit,
    onPickDocument: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AttachmentAction(Icons.Default.PhotoLibrary, "图片", Color(0xFF43A047), onPickImages)
        AttachmentAction(Icons.Default.Videocam, "视频", Color(0xFF1E88E5), onPickVideo)
        AttachmentAction(Icons.Default.InsertDriveFile, "文件", Color(0xFFFB8C00), onPickDocument)
    }
}

@Composable
private fun AttachmentAction(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun VoiceRecordButton(
    recording: Boolean,
    onStart: () -> Boolean,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    val currentOnCancel by rememberUpdatedState(onCancel)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (recording) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    val started = currentOnStart()
                    if (!started) return@awaitEachGesture

                    var cancelled = false
                    val downY = down.position.y
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            cancelled = downY - change.position.y > 80.dp.toPx()
                        }
                        change.consume()
                    } while (event.changes.any { it.pressed })

                    if (cancelled) currentOnCancel() else currentOnStop()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "按住说话",
            tint = if (recording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AudioMessageBubble(
    url: String,
    isSelf: Boolean,
    durationSeconds: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    fun stopPlayback() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
        isPlaying = false
    }

    DisposableEffect(Unit) {
        onDispose { stopPlayback() }
    }

    fun togglePlayback() {
        if (isPlaying) {
            stopPlayback()
            return
        }
        val mediaPlayer = MediaPlayer()
        player = mediaPlayer
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mediaPlayer.setDataSource(url)
            mediaPlayer.setOnPreparedListener {
                it.start()
                isPlaying = true
            }
            mediaPlayer.setOnCompletionListener { stopPlayback() }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                stopPlayback()
                true
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            stopPlayback()
            Toast.makeText(context, "无法播放语音", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { togglePlayback() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "停止" else "播放",
            tint = if (isSelf) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(14) { index ->
                val barHeight = (4 + (index % 5) * 4).dp
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            (if (isSelf) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)
                                .copy(alpha = 0.6f)
                        )
                )
            }
        }
        if (durationSeconds > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60),
                fontSize = 11.sp,
                color = if (isSelf) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
