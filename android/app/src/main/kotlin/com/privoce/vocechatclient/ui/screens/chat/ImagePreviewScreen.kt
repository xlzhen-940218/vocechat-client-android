package com.privoce.vocechatclient.ui.screens.chat

import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.data.models.ChatMessage
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import kotlinx.coroutines.delay

/**
 * Full screen media viewer with pinch/double-tap zoom for images, in-app video
 * playback, and horizontal paging across all images and videos of the chat.
 */
@Composable
fun MediaPreviewScreen(
    items: List<ChatMessage>,
    initialIndex: Int,
    repository: VoceChatRepository,
    onClose: () -> Unit
) {
    if (items.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    var zoomed by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, items.lastIndex)
    ) { items.size }

    BackHandler(onBack = onClose)

    LaunchedEffect(pagerState.currentPage) {
        zoomed = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !zoomed,
            modifier = Modifier.fillMaxSize(),
            key = { page ->
                items[page].localMid.ifBlank { items[page].mid.toString() }
            }
        ) { page ->
            val message = items[page]
            val url = repository.apiClient.getFileUrl(message.content)
            if (message.isVideo) {
                VideoPreviewPage(
                    url = url,
                    isActive = page == pagerState.currentPage
                )
            } else {
                ZoomableImage(
                    url = url,
                    onZoomedChange = { isZoomed ->
                        if (page == pagerState.currentPage) zoomed = isZoomed
                    },
                    onTapClose = onClose
                )
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.action_close_preview),
                tint = Color.White
            )
        }

        if (items.size > 1) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${items.size}",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun VideoPreviewPage(
    url: String,
    isActive: Boolean
) {
    val context = LocalContext.current
    val videoView = remember(url) { VideoView(context) }
    var isPlaying by remember { mutableStateOf(false) }
    var prepared by remember { mutableStateOf(false) }
    var durationMs by remember { mutableIntStateOf(0) }
    var positionMs by remember { mutableIntStateOf(0) }
    val activeState by rememberUpdatedState(isActive)

    DisposableEffect(videoView) {
        onDispose {
            try {
                videoView.stopPlayback()
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(url) {
        videoView.setMediaController(null)
        videoView.setOnPreparedListener { mediaPlayer ->
            durationMs = mediaPlayer.duration
            mediaPlayer.isLooping = true
            prepared = true
            if (activeState) {
                videoView.start()
                isPlaying = true
            }
        }
        videoView.setOnCompletionListener {
            isPlaying = false
            positionMs = 0
            videoView.seekTo(0)
        }
        videoView.setOnErrorListener { _, _, _ ->
            isPlaying = false
            Toast.makeText(context, context.getString(R.string.error_play_video), Toast.LENGTH_SHORT).show()
            true
        }
        try {
            videoView.setVideoURI(Uri.parse(url))
        } catch (_: Exception) {
            Toast.makeText(context, context.getString(R.string.error_play_video), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isActive, prepared) {
        if (!isActive) {
            if (videoView.isPlaying) videoView.pause()
            isPlaying = false
        } else if (prepared && !videoView.isPlaying) {
            videoView.start()
            isPlaying = true
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            positionMs = videoView.currentPosition
            delay(200)
        }
    }

    fun togglePlayback() {
        if (!prepared) return
        if (videoView.isPlaying) {
            videoView.pause()
            isPlaying = false
        } else {
            videoView.start()
            isPlaying = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { videoView },
            modifier = Modifier.fillMaxSize()
        )

        if (!prepared) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
            )
        }

        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.45f),
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .clickable { togglePlayback() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.chat_audio_pause else R.string.chat_audio_play
                    ),
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            Slider(
                value = positionMs.toFloat(),
                onValueChange = { value ->
                    positionMs = value.toInt()
                    if (prepared) videoView.seekTo(positionMs)
                },
                valueRange = 0f..durationMs.coerceAtLeast(1).toFloat(),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatMediaTime(positionMs),
                    color = Color.White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatMediaTime(durationMs),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.chat_attachment_video),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    url: String,
    onZoomedChange: (Boolean) -> Unit,
    onTapClose: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewWidth = constraints.maxWidth.toFloat()
        val viewHeight = constraints.maxHeight.toFloat()

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.action_view_image),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            // Only claim the gesture when pinching or already zoomed in,
                            // otherwise leave horizontal drags to the pager.
                            val shouldHandle = zoomChange != 1f || scale > 1.01f
                            if (shouldHandle) {
                                val newScale = (scale * zoomChange).coerceIn(1f, 6f)
                                scale = newScale
                                if (newScale <= 1.01f) {
                                    offset = Offset.Zero
                                } else {
                                    val maxX = (newScale - 1f) * viewWidth / 2f
                                    val maxY = (newScale - 1f) * viewHeight / 2f
                                    offset = Offset(
                                        (offset.x + panChange.x).coerceIn(-maxX, maxX),
                                        (offset.y + panChange.y).coerceIn(-maxY, maxY)
                                    )
                                }
                                onZoomedChange(newScale > 1.01f)
                                event.changes.forEach { it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (scale > 1.01f) {
                                scale = 1f
                                offset = Offset.Zero
                                onZoomedChange(false)
                            } else {
                                onTapClose()
                            }
                        },
                        onDoubleTap = {
                            if (scale > 1.01f) {
                                scale = 1f
                                offset = Offset.Zero
                                onZoomedChange(false)
                            } else {
                                scale = 2.5f
                                onZoomedChange(true)
                            }
                        }
                    )
                }
        )
    }
}
