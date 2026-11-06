package com.privoce.vocechatclient.ui.screens.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.data.models.ChatMessage
import com.privoce.vocechatclient.data.repository.VoceChatRepository

/**
 * Full screen image viewer with pinch/double-tap zoom and horizontal paging
 * across all images of the current chat.
 */
@Composable
fun ImagePreviewScreen(
    images: List<ChatMessage>,
    initialIndex: Int,
    repository: VoceChatRepository,
    onClose: () -> Unit
) {
    if (images.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    var zoomed by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, images.lastIndex)
    ) { images.size }

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
                images[page].localMid.ifBlank { images[page].mid.toString() }
            }
        ) { page ->
            val url = repository.apiClient.getFileUrl(images[page].content)
            ZoomableImage(
                url = url,
                onZoomedChange = { isZoomed ->
                    if (page == pagerState.currentPage) zoomed = isZoomed
                },
                onTapClose = onClose
            )
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

        if (images.size > 1) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
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
