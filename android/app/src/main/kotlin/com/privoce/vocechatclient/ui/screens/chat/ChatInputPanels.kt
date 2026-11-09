package com.privoce.vocechatclient.ui.screens.chat

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privoce.vocechatclient.R
import kotlinx.coroutines.delay
import kotlin.random.Random

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

/**
 * 全局语音播放控制：同一时刻只允许一条语音消息播放。
 * 播放新语音时会先暂停上一条，避免多条语音同时出声。
 */
private object AudioPlaybackManager {
    private var currentMid: Long = -1L
    private var pauseCurrent: (() -> Unit)? = null

    fun onStart(mid: Long, pause: () -> Unit) {
        if (currentMid != mid) {
            pauseCurrent?.invoke()
            currentMid = mid
            pauseCurrent = pause
        }
    }

    fun onStop(mid: Long) {
        if (currentMid == mid) {
            currentMid = -1L
            pauseCurrent = null
        }
    }
}

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
    onCamera: () -> Unit,
    onRecordVideo: () -> Unit,
    onPickImages: () -> Unit,
    onPickVideo: () -> Unit,
    onPickDocument: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AttachmentAction(
            Icons.Default.PhotoCamera,
            stringResource(R.string.chat_attachment_camera),
            Color(0xFF8E24AA),
            onCamera,
            Modifier.weight(1f)
        )
        AttachmentAction(
            Icons.Default.PhotoLibrary,
            stringResource(R.string.chat_attachment_image),
            Color(0xFF43A047),
            onPickImages,
            Modifier.weight(1f)
        )
        AttachmentAction(
            Icons.Default.Videocam,
            stringResource(R.string.chat_attachment_record_video),
            Color(0xFFE53935),
            onRecordVideo,
            Modifier.weight(1f)
        )
        AttachmentAction(
            Icons.Default.VideoLibrary,
            stringResource(R.string.chat_attachment_video),
            Color(0xFF1E88E5),
            onPickVideo,
            Modifier.weight(1f)
        )
        AttachmentAction(
            Icons.AutoMirrored.Filled.InsertDriveFile,
            stringResource(R.string.chat_attachment_file),
            Color(0xFFFB8C00),
            onPickDocument,
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun AttachmentAction(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
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
            contentDescription = stringResource(R.string.chat_voice_hold_to_talk),
            tint = if (recording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AudioMessageBubble(
    url: String,
    isSelf: Boolean,
    durationSeconds: Int,
    messageMid: Long = 0L,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var positionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(durationSeconds * 1000) }
    val waveform = remember(url) {
        val random = Random(url.hashCode().toLong())
        List(32) { 0.25f + random.nextFloat() * 0.75f }
    }

    val activeColor = if (isSelf) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    val inactiveColor = activeColor.copy(alpha = 0.25f)

    fun releasePlayer() {
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
        isPlaying = false
        positionMs = 0
        AudioPlaybackManager.onStop(messageMid)
    }

    /** 被其他语音抢占播放时，只暂停当前播放器，保留进度。 */
    fun pauseForOthers() {
        try {
            player?.let { if (it.isPlaying) it.pause() }
        } catch (_: Exception) {
        }
        isPlaying = false
    }

    DisposableEffect(Unit) {
        onDispose { releasePlayer() }
    }

    fun togglePlayback() {
        player?.let { existing ->
            if (existing.isPlaying) {
                existing.pause()
                isPlaying = false
                AudioPlaybackManager.onStop(messageMid)
            } else {
                AudioPlaybackManager.onStart(messageMid) { pauseForOthers() }
                existing.start()
                isPlaying = true
            }
            return
        }
        val mediaPlayer = MediaPlayer()
        player = mediaPlayer
        AudioPlaybackManager.onStart(messageMid) { pauseForOthers() }
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mediaPlayer.setDataSource(url)
            mediaPlayer.setOnPreparedListener {
                if (it.duration > 0) durationMs = it.duration
                it.start()
                isPlaying = true
            }
            mediaPlayer.setOnCompletionListener {
                isPlaying = false
                positionMs = 0
                it.seekTo(0)
                AudioPlaybackManager.onStop(messageMid)
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                releasePlayer()
                Toast.makeText(context, context.getString(R.string.error_play_audio), Toast.LENGTH_SHORT).show()
                true
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            releasePlayer()
            Toast.makeText(context, context.getString(R.string.error_play_audio), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            player?.let { if (it.isPlaying) positionMs = it.currentPosition }
            delay(120)
        }
    }

    val progress = if (durationMs > 0) {
        (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .width(220.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = activeColor,
            modifier = Modifier.size(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clickable { togglePlayback() }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.chat_audio_pause else R.string.chat_audio_play
                    ),
                    tint = if (isSelf) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .pointerInput(durationMs) {
                        detectTapGestures { offset ->
                            if (durationMs > 0) {
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                val target = (durationMs * fraction).toInt()
                                player?.seekTo(target)
                                positionMs = target
                            }
                        }
                    }
            ) {
                val count = waveform.size
                val gap = 2.dp.toPx()
                val barWidth = ((size.width - gap * (count - 1)) / count).coerceAtLeast(1f)
                val progressX = size.width * progress
                waveform.forEachIndexed { index, amplitude ->
                    val barHeight = (size.height * amplitude).coerceAtLeast(3.dp.toPx())
                    val x = index * (barWidth + gap)
                    val played = x + barWidth / 2f <= progressX
                    drawRoundRect(
                        color = if (played) activeColor else inactiveColor,
                        topLeft = Offset(x, (size.height - barHeight) / 2f),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMediaTime(positionMs),
                    fontSize = 10.sp,
                    color = activeColor.copy(alpha = 0.75f)
                )
                Text(
                    text = formatMediaTime(durationMs),
                    fontSize = 10.sp,
                    color = activeColor.copy(alpha = 0.55f)
                )
            }
        }
    }
}

internal fun formatMediaTime(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
}
