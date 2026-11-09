package com.privoce.vocechatclient.ui.screens.chat

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.data.models.ChatMessage
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

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
    ImmersiveSystemBars()

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
    var isSeeking by remember { mutableStateOf(false) }
    val activeState by rememberUpdatedState(isActive)

    DisposableEffect(videoView) {
        onDispose {
            try {
                videoView.stopPlayback()
            } catch (_: Exception) {
            }
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

    LaunchedEffect(isPlaying, isSeeking) {
        // 拖动进度条时暂停刷新，避免进度条被回写导致“跳回”
        if (isSeeking) return@LaunchedEffect
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { togglePlayback() }
    ) {
        AndroidView(
            factory = {
                // 在 View 真正挂载后再设置数据源，避免 Surface 尚未创建导致黑屏或播放失败
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
                videoView
            },
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

        // 仅在暂停时显示播放按钮，避免遮挡视频画面
        if (prepared && !isPlaying) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.chat_audio_play),
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
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
                    isSeeking = true
                    positionMs = value.toInt()
                    if (prepared) videoView.seekTo(positionMs)
                },
                onValueChangeFinished = { isSeeking = false },
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

                            // 缩放/拖动时才拦截手势；单纯点击不拦截，保证点击可退出或还原缩放
                            val isPinch = zoomChange != 1f && event.changes.size > 1
                            val isPan = panChange.getDistance() > 2f
                            val shouldHandle = isPinch || (scale > 1.01f && isPan)
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

/**
 * 内置 PDF 预览：先用系统 PdfRenderer 把每页渲染成位图，再按页纵向展示。
 * 这样可以避免跳转外部应用，也无需引入第三方 PDF 库。
 */
@Composable
fun PdfPreviewDialog(
    file: File,
    title: String,
    onClose: () -> Unit
) {
    var pageCount by remember(file) { mutableStateOf<Int?>(null) }
    var failed by remember(file) { mutableStateOf(false) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    // 以屏幕宽度作为渲染宽度，保证文字清晰度
    val targetWidthPx = remember(density, configuration) {
        with(density) { configuration.screenWidthDp.dp.roundToPx() }.coerceAtLeast(720)
    }

    LaunchedEffect(file) {
        val count = withContext(Dispatchers.IO) {
            runCatching {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer -> renderer.pageCount }
                }
            }.getOrNull()
        }
        pageCount = count
        failed = count == null
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(color = Color(0xFF1B1B1B), modifier = Modifier.fillMaxSize()) {
            ImmersiveSystemBars()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_close_preview),
                            tint = Color.White
                        )
                    }
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    )
                    pageCount?.let { count ->
                        Text(
                            text = "$count",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                val count = pageCount
                when {
                    failed -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.chat_pdf_preview_failed),
                            color = Color.White
                        )
                    }

                    count == null -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(count) { index ->
                            PdfPageItem(
                                file = file,
                                pageIndex = index,
                                targetWidthPx = targetWidthPx
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 单独渲染一页，避免一次性把所有页面位图都放进内存。 */
@Composable
private fun PdfPageItem(
    file: File,
    pageIndex: Int,
    targetWidthPx: Int
) {
    val pageBitmap by produceState<Bitmap?>(null, file, pageIndex, targetWidthPx) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        renderer.openPage(pageIndex).use { page ->
                            val scale = targetWidthPx.toFloat() / page.width
                            val height = (page.height * scale).toInt().coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(
                                targetWidthPx,
                                height,
                                Bitmap.Config.ARGB_8888
                            )
                            bitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                            bitmap
                        }
                    }
                }
            }.getOrNull()
        }
    }

    val bitmap = pageBitmap
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
        }
    }
}

/**
 * 预览页沉浸式全屏：
 * 1. 强制 Dialog 窗口铺满整个屏幕（默认不会被状态栏区域裁掉）；
 * 2. 隐藏状态栏与导航栏，从边缘滑动可临时唤出；
 * 3. 关闭预览时恢复系统栏，避免影响聊天等其他页面。
 */
@Composable
private fun ImmersiveSystemBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        var appliedWindow: Window? = null
        var appliedController: WindowInsetsControllerCompat? = null

        fun applyTo(window: Window) {
            if (appliedWindow != null) return
            appliedWindow = window
            // Dialog 窗口默认会避开状态栏，这里允许内容绘制到系统栏下方并铺满屏幕
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            appliedController = controller
        }

        // Dialog 刚创建时视图可能还未挂到窗口上，找不到时等一帧再试
        val dialogWindow = (view.rootView as? DialogWindowProvider)?.window
        if (dialogWindow != null) {
            applyTo(dialogWindow)
        } else {
            view.post {
                val window = (view.rootView as? DialogWindowProvider)?.window ?: return@post
                applyTo(window)
            }
        }

        onDispose {
            appliedWindow?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            appliedController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
