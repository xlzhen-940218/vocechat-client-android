package com.privoce.vocechatclient.ui.screens.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.data.models.ChatMessage
import com.privoce.vocechatclient.data.models.MessageStatus
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import com.privoce.vocechatclient.ui.components.VoceAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    chatType: String, // "channel" or "dm"
    chatId: Int,
    repository: VoceChatRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentUserId = repository.preferences.currentUserId
    val messagesMap by repository.messages.collectAsState()
    val channels by repository.channels.collectAsState()
    val users by repository.users.collectAsState()

    val channel = remember(channels, chatId) {
        if (chatType == "channel") channels.find { it.gid == chatId } else null
    }
    val dmUser = remember(users, chatId) {
        if (chatType == "dm") users.find { it.uid == chatId } else null
    }

    val title = channel?.displayName ?: dmUser?.displayName ?: stringResource(R.string.chat_fallback_title, chatId)
    val subtitle = if (chatType == "channel") {
        stringResource(R.string.chat_members_count, channel?.members?.size ?: 0)
    } else {
        stringResource(if (dmUser?.online == true) R.string.user_online else R.string.user_offline)
    }

    val key = "${chatType}_$chatId"
    val messageList = messagesMap[key] ?: emptyList()
    val mediaMessages = remember(messageList) { messageList.filter { it.isImage || it.isVideo } }

    // 聊天内消息搜索
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    val displayMessages = remember(messageList, searchQuery) {
        if (searchQuery.isBlank()) {
            messageList
        } else {
            messageList.filter {
                it.content.contains(searchQuery, ignoreCase = true) ||
                    it.fileName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var inputText by remember { mutableStateOf("") }
    var replyingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }
    var previewTarget by remember { mutableStateOf<ChatMessage?>(null) }
    var showExtras by remember { mutableStateOf(false) }
    var showEmoji by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordStartAt by remember { mutableLongStateOf(0L) }
    var recordElapsed by remember { mutableIntStateOf(0) }

    // @提及：已选中的成员（uid → 显示名），发送时作为消息属性上报
    val mentionedUsers = remember { mutableStateMapOf<Int, String>() }
    // 输入框光标前的 "@关键词"，用于筛选可提及的成员
    val mentionQuery: String? = remember(inputText) {
        val atIndex = inputText.lastIndexOf('@')
        val after = if (atIndex >= 0) inputText.substring(atIndex + 1) else ""
        if (atIndex >= 0 && after.none { it.isWhitespace() }) after else null
    }
    val mentionCandidates = remember(mentionQuery, channel, users) {
        if (chatType != "channel" || mentionQuery == null) {
            emptyList()
        } else {
            (channel?.members ?: emptyList())
                .mapNotNull { uid -> users.find { it.uid == uid } }
                .filter { it.uid != currentUserId }
                .filter { mentionQuery.isBlank() || it.displayName.contains(mentionQuery, ignoreCase = true) }
                .take(8)
        }
    }

    // 文档预览相关状态
    var pdfPreviewFile by remember { mutableStateOf<File?>(null) }
    var pdfPreviewLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val voiceRecorder = remember { VoiceRecorder(context) }

    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
        if (!granted) {
            Toast.makeText(context, context.getString(R.string.chat_error_microphone_permission), Toast.LENGTH_SHORT).show()
        }
    }

    fun sendPickedFile(uri: Uri, fallbackMime: String = "application/octet-stream") {
        // 读取/拷贝文件可能很耗时，放到 IO 线程，避免卡住输入界面
        scope.launch {
            val prepared = withContext(Dispatchers.IO) {
                runCatching {
                    val mime = context.contentResolver.getType(uri) ?: fallbackMime
                    val name = getDisplayName(context, uri)
                    val safeName = name.substringAfterLast('/').substringAfterLast('\\')
                    val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}_$safeName")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    } ?: error("Unable to open input stream for $uri")
                    Triple(file, mime, name)
                }.getOrElse { e ->
                    Log.e("ChatScreen", "Failed to copy picked file", e)
                    null
                }
            }
            if (prepared == null) {
                Toast.makeText(context, context.getString(R.string.chat_error_read_file), Toast.LENGTH_SHORT).show()
                return@launch
            }
            repository.sendAttachment(chatType, chatId, prepared.first, prepared.second, prepared.third)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(9)
    ) { uris ->
        uris.forEach { uri -> sendPickedFile(uri, "image/*") }
    }
    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { sendPickedFile(it, "video/mp4") }
    }
    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { sendPickedFile(it) }
    }

    // 拍照：先创建缓存文件并通过 FileProvider 暴露给相机应用
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = cameraImageUri
        cameraImageUri = null
        if (success && captured != null) {
            sendPickedFile(captured, "image/jpeg")
        }
    }

    fun launchCamera() {
        try {
            val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("ChatScreen", "Failed to launch camera", e)
            Toast.makeText(context, context.getString(R.string.chat_error_read_file), Toast.LENGTH_SHORT).show()
        }
    }

    // 录像：与拍照一致，先准备缓存文件并通过 FileProvider 交给系统相机
    var cameraVideoUri by remember { mutableStateOf<Uri?>(null) }
    val videoCaptureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { success ->
        val captured = cameraVideoUri
        cameraVideoUri = null
        if (success && captured != null) {
            sendPickedFile(captured, "video/mp4")
        }
    }

    fun launchVideoCapture() {
        try {
            val file = File(context.cacheDir, "camera_video_${System.currentTimeMillis()}.mp4")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            cameraVideoUri = uri
            videoCaptureLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("ChatScreen", "Failed to launch video capture", e)
            Toast.makeText(context, context.getString(R.string.chat_error_read_file), Toast.LENGTH_SHORT).show()
        }
    }

    // 选中成员后把输入框中的 "@关键词" 替换为 "@显示名 "
    fun insertMention(user: com.privoce.vocechatclient.data.models.UserInfo) {
        val atIndex = inputText.lastIndexOf('@')
        if (atIndex < 0) return
        inputText = inputText.substring(0, atIndex) + "@${user.displayName} "
        mentionedUsers[user.uid] = user.displayName
    }

    fun startRecording(): Boolean {
        if (!hasRecordPermission) {
            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return false
        }
        if (!voiceRecorder.start()) {
            Toast.makeText(context, context.getString(R.string.chat_error_start_recording), Toast.LENGTH_SHORT).show()
            return false
        }
        isRecording = true
        recordStartAt = System.currentTimeMillis()
        recordElapsed = 0
        return true
    }

    fun stopRecording() {
        if (!voiceRecorder.isActive) return
        val result = voiceRecorder.stop()
        isRecording = false
        if (result == null) {
            Toast.makeText(context, context.getString(R.string.chat_error_recording_failed), Toast.LENGTH_SHORT).show()
            return
        }
        if (result.durationMs < 800) {
            result.file.delete()
            Toast.makeText(context, context.getString(R.string.chat_error_recording_too_short), Toast.LENGTH_SHORT).show()
            return
        }
        repository.sendAttachment(
            chatType = chatType,
            id = chatId,
            file = result.file,
            mimeType = "audio/mp4",
            displayName = "voice_${System.currentTimeMillis()}.m4a",
            extraProperties = mapOf("duration" to (result.durationMs / 1000).toInt())
        )
    }

    fun cancelRecording() {
        voiceRecorder.cancel()
        isRecording = false
    }

    DisposableEffect(Unit) {
        onDispose { voiceRecorder.cancel() }
    }

    // Messages arriving in this chat should not count as unread while it is open.
    DisposableEffect(key) {
        repository.setActiveChat(key)
        onDispose { repository.setActiveChat(null) }
    }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            recordElapsed = ((System.currentTimeMillis() - recordStartAt) / 1000).toInt()
            if (recordElapsed >= 60) {
                stopRecording()
                break
            }
            delay(200)
        }
    }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            searchFocusRequester.requestFocus()
        }
    }

    // 面板打开时，返回键优先收起面板而不是退出聊天
    BackHandler(enabled = showEmoji || showExtras) {
        showEmoji = false
        showExtras = false
    }

    fun openFile(filePath: String) {
        // PDF 走内置预览：先下载到缓存，再由 PdfRenderer 逐页渲染
        if (filePath.endsWith(".pdf", ignoreCase = true)) {
            if (pdfPreviewLoading) return
            pdfPreviewLoading = true
            scope.launch {
                val dest = File(context.cacheDir, "preview_${System.currentTimeMillis()}.pdf")
                val result = withContext(Dispatchers.IO) {
                    repository.apiClient.downloadFile(filePath, dest)
                }
                pdfPreviewLoading = false
                result.onSuccess { pdfPreviewFile = it }
                    .onFailure {
                        Toast.makeText(
                            context,
                            context.getString(R.string.chat_pdf_preview_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            return
        }
        try {
            val fileUrl = repository.apiClient.getFileUrl(filePath)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.chat_error_open_attachment, e.message ?: ""),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Auto-load history for channel or DM
    LaunchedEffect(chatId, chatType) {
        if (chatType == "channel") {
            repository.loadChannelHistory(chatId)
        } else if (chatType == "dm") {
            repository.loadDmHistory(chatId)
        }
    }

    // Auto-scroll to bottom on first load, on own messages, or when already at the bottom.
    var didInitialScroll by remember(key) { mutableStateOf(false) }
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(messageList.size) {
        if (isSearching) return@LaunchedEffect
        if (messageList.isEmpty()) return@LaunchedEffect
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        val nearBottom = lastVisibleIndex >= messageList.lastIndex - 1
        val sentByMe = messageList.last().fromUid == currentUserId
        if (!didInitialScroll || nearBottom || sentByMe) {
            listState.animateScrollToItem(messageList.lastIndex)
            didInitialScroll = true
        }
    }
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) return@LaunchedEffect
        // 键盘弹出时收起表情/附件面板，避免和输入法同时占用屏幕
        showEmoji = false
        showExtras = false
        if (messageList.isEmpty()) return@LaunchedEffect
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        if (lastVisibleIndex >= messageList.lastIndex - 1) {
            listState.animateScrollToItem(messageList.lastIndex)
        }
    }

    fun sendMessage() {
        if (inputText.isBlank()) return
        val text = inputText.trim()
        val replyMid = replyingMessage?.mid
        // 只上报正文中仍然保留的 @成员，避免用户删掉提及后仍触发通知
        val mentions = mentionedUsers
            .filter { (_, name) -> text.contains("@$name") }
            .keys
            .toList()
        inputText = ""
        replyingMessage = null
        mentionedUsers.clear()
        repository.sendMessage(chatType, chatId, text, replyMid, mentions)
    }

    // Message context options sheet
    selectedMessageForOptions?.let { msg ->
        ModalBottomSheet(
            onDismissRequest = { selectedMessageForOptions = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.chat_message_actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Quick Reactions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("👍", "❤️", "😄", "🎉", "🔥", "👀").forEach { emoji ->
                        Surface(
                            onClick = {
                                repository.reactMessage(msg.mid, emoji)
                                selectedMessageForOptions = null
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ListItem(
                    headlineContent = { Text(stringResource(R.string.chat_reply)) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null) },
                    modifier = Modifier.clickable {
                        replyingMessage = msg
                        selectedMessageForOptions = null
                    }
                )

                if (msg.fromUid == currentUserId && msg.status == MessageStatus.FAILED) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.chat_action_retry)) },
                        leadingContent = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        modifier = Modifier.clickable {
                            repository.retryMessage(msg)
                            selectedMessageForOptions = null
                        }
                    )
                }

                if (msg.fromUid == currentUserId) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.chat_delete_message), color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            repository.deleteMessage(msg.mid)
                            selectedMessageForOptions = null
                        }
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    stringResource(R.string.chat_search_placeholder),
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                        if (chatType == "channel") {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (channel?.isPublic == true) Icons.Default.Tag else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            VoceAvatar(
                                avatarUrl = repository.apiClient.getUserAvatarUrl(chatId, dmUser?.avatarUpdatedAt),
                                name = title,
                                size = 38.dp,
                                showOnlineBadge = true,
                                isOnline = dmUser?.online == true,
                                isBot = dmUser?.isBot == true
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSearching) {
                            isSearching = false
                            searchQuery = ""
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.chat_back))
                    }
                },
                actions = {
                    if (!isSearching) {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.chat_search_messages)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                // 先处理导航栏 inset，再处理输入法 inset，两者会自动去重，避免底部多出一截空白
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (isSearching && displayMessages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight(0.6f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.chat_search_no_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                itemsIndexed(
                    items = displayMessages,
                    key = { _, msg -> msg.localMid.ifBlank { msg.mid.toString() } }
                ) { index, msg ->
                    val isSelf = msg.fromUid == currentUserId
                    val sender = repository.getUser(msg.fromUid)
                    val previous = displayMessages.getOrNull(index - 1)

                    // 同一人连续发送时只在该组第一条显示昵称
                    val showSenderHeader = previous == null ||
                        previous.fromUid != msg.fromUid ||
                        msg.createdAt - previous.createdAt > 5 * 60 * 1000 ||
                        !isSameDay(previous.createdAt, msg.createdAt)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 跨天时插入日期分隔条，避免每条消息都重复显示完整日期
                        if (previous == null || !isSameDay(previous.createdAt, msg.createdAt)) {
                            DateSeparator(timestamp = msg.createdAt)
                        }

                        MessageBubbleItem(
                            message = msg,
                            isSelf = isSelf,
                            showSenderHeader = showSenderHeader,
                            senderName = sender?.displayName ?: if (isSelf) {
                                stringResource(R.string.chat_self_name)
                            } else {
                                stringResource(R.string.fallback_user_name, msg.fromUid)
                            },
                            senderAvatarUrl = repository.apiClient.getUserAvatarUrl(msg.fromUid, sender?.avatarUpdatedAt),
                            senderIsBot = sender?.isBot == true,
                            onLongClick = { selectedMessageForOptions = msg },
                            onPreviewMedia = { target ->
                                keyboardController?.hide()
                                previewTarget = target
                            },
                            onOpenFile = { path -> openFile(path) },
                            repository = repository
                        )
                    }
                }
            }

            // Replying Banner
            AnimatedVisibility(visible = replyingMessage != null) {
                replyingMessage?.let { replyMsg ->
                    val replySender = repository.getUser(replyMsg.fromUid)
                    val replySenderName = replySender?.displayName
                        ?: stringResource(R.string.fallback_user_name, replyMsg.fromUid)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Reply,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.chat_replying_to, replySenderName),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = replyMsg.previewText(context),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { replyingMessage = null }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.chat_cancel_reply),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 输入 "@" 时在输入框上方显示可提及成员
            AnimatedVisibility(visible = mentionCandidates.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = stringResource(R.string.chat_mention),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        mentionCandidates.forEach { user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { insertMention(user) }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                VoceAvatar(
                                    avatarUrl = repository.apiClient.getUserAvatarUrl(user.uid, user.avatarUpdatedAt),
                                    name = user.displayName,
                                    size = 28.dp,
                                    isBot = user.isBot
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Recording indicator
            AnimatedVisibility(visible = isRecording) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.chat_recording, recordElapsed),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.chat_recording_hint),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Input Bar + attachment / emoji panels
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                // 加号面板与表情面板互斥，避免两个面板状态不同步
                                if (showEmoji) {
                                    showEmoji = false
                                    showExtras = true
                                } else {
                                    showExtras = !showExtras
                                }
                                if (showExtras) {
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (showExtras || showEmoji) Icons.Default.Close else Icons.Default.AddCircleOutline,
                                contentDescription = stringResource(R.string.chat_add_attachment),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text(stringResource(R.string.chat_input_placeholder), fontSize = 14.sp) },
                            maxLines = 4,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        showExtras = false
                                        showEmoji = false
                                    }
                                }
                        )

                        if (chatType == "channel") {
                            IconButton(
                                onClick = {
                                    if (!inputText.endsWith("@")) {
                                        inputText += "@"
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.AlternateEmail,
                                    contentDescription = stringResource(R.string.chat_mention),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                showEmoji = !showEmoji
                                showExtras = false
                                if (showEmoji) keyboardController?.hide()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEmotions,
                                contentDescription = stringResource(R.string.chat_emoji),
                                tint = if (showEmoji) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = { sendMessage() },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.chat_send),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            VoiceRecordButton(
                                recording = isRecording,
                                onStart = { startRecording() },
                                onStop = { stopRecording() },
                                onCancel = { cancelRecording() }
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = showEmoji) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EmojiPanel(onEmojiSelected = { emoji -> inputText += emoji })
                    }
                }

                AnimatedVisibility(visible = showExtras && !showEmoji) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AttachmentPanel(
                            onCamera = {
                                showExtras = false
                                launchCamera()
                            },
                            onRecordVideo = {
                                showExtras = false
                                launchVideoCapture()
                            },
                            onPickImages = {
                                showExtras = false
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onPickVideo = {
                                showExtras = false
                                videoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            onPickDocument = {
                                showExtras = false
                                documentPicker.launch(arrayOf("*/*"))
                            }
                        )
                    }
                }
            }
        }
    }

        // Fullscreen image/media preview with zoom & paging across all chat media
        previewTarget?.let { target ->
            val isSameMessage = { msg: ChatMessage ->
                (msg.mid != 0L && msg.mid == target.mid) ||
                    (msg.localMid.isNotBlank() && msg.localMid == target.localMid)
            }
            val previewItems = mediaMessages.takeIf { list -> list.any(isSameMessage) } ?: listOf(target)
            val startIndex = previewItems.indexOfFirst(isSameMessage).coerceAtLeast(0)

            MediaPreviewScreen(
                items = previewItems,
                initialIndex = startIndex,
                repository = repository,
                onClose = { previewTarget = null }
            )
        }

        // 内置 PDF 预览
        pdfPreviewFile?.let { file ->
            PdfPreviewDialog(
                file = file,
                title = file.name,
                onClose = { pdfPreviewFile = null }
            )
        }

        if (pdfPreviewLoading) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Surface(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.chat_pdf_preview_loading))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubbleItem(
    message: ChatMessage,
    isSelf: Boolean,
    showSenderHeader: Boolean,
    senderName: String,
    senderAvatarUrl: String?,
    senderIsBot: Boolean = false,
    onLongClick: () -> Unit,
    onPreviewMedia: (ChatMessage) -> Unit,
    onOpenFile: (String) -> Unit,
    repository: VoceChatRepository
) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.createdAt) {
        timeFormat.format(Date(message.createdAt))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isSelf) {
            VoceAvatar(
                avatarUrl = senderAvatarUrl,
                name = senderName,
                size = 36.dp,
                isBot = senderIsBot,
                modifier = Modifier.padding(top = 2.dp, end = 8.dp)
            )
        }

        Column(
            horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            if (!isSelf && showSenderHeader) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            // Bubble Card Container
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isSelf) 16.dp else 4.dp,
                    topEnd = if (isSelf) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (isSelf) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp,
                modifier = Modifier.combinedClickable(
                    onClick = {
                        if (message.isImage || message.isVideo) {
                            onPreviewMedia(message)
                        } else if (message.isFile) {
                            onOpenFile(message.content)
                        }
                    },
                    onLongClick = onLongClick
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    // Quoted Reply Card ("包含关系")
                    if (message.replyToMid != null && message.replyToMid > 0) {
                        val repliedMsg = repository.getMessage(message.replyToMid)
                        val repliedSender = repliedMsg?.let { repository.getUser(it.fromUid) }
                        val repliedSenderName = repliedSender?.displayName
                            ?: stringResource(R.string.fallback_user_name, repliedMsg?.fromUid ?: message.replyToMid)
                        val repliedAvatarUrl = repliedSender?.let { repository.apiClient.getUserAvatarUrl(it.uid, it.avatarUpdatedAt) }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelf) MaterialTheme.colorScheme.surface.copy(alpha = 0.75f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    VoceAvatar(
                                        avatarUrl = repliedAvatarUrl,
                                        name = repliedSenderName,
                                        size = 18.dp,
                                        isBot = repliedSender?.isBot == true
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = repliedSenderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (repliedMsg != null) {
                                    if (repliedMsg.isImage) {
                                        val imgUrl = repository.apiClient.getFileUrl(repliedMsg.content, thumbnail = true)
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(imgUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = stringResource(R.string.chat_quoted_image),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { onPreviewMedia(repliedMsg) }
                                        )
                                    } else if (repliedMsg.isAudio) {
                                        Text(
                                            text = repliedMsg.previewText(context),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (repliedMsg.isVideo) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { onPreviewMedia(repliedMsg) }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(64.dp)
                                                    .height(48.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.Black),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(repository.apiClient.getFileUrl(repliedMsg.content))
                                                        .videoFrameMillis(1000)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = stringResource(R.string.chat_attachment_video),
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = repliedMsg.fileName,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else if (repliedMsg.isFile) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { onOpenFile(repliedMsg.content) }
                                        ) {
                                            Icon(
                                                Icons.Default.AttachFile,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = repliedMsg.fileName,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = repliedMsg.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Text(
                                        text = stringResource(R.string.chat_replied_message, message.replyToMid),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // Message Content
                    if (message.isImage) {
                        val imageUrl = repository.apiClient.getFileUrl(message.content, thumbnail = true)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.chat_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 260.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else if (message.isAudio) {
                        AudioMessageBubble(
                            url = repository.apiClient.getFileUrl(message.content),
                            isSelf = isSelf,
                            durationSeconds = message.durationSeconds,
                            messageMid = message.mid
                        )
                    } else if (message.isVideo) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(repository.apiClient.getFileUrl(message.content))
                                    .videoFrameMillis(1000)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = stringResource(R.string.chat_attachment_video),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                        )
                                    )
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = stringResource(R.string.chat_audio_play),
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = message.fileName,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (message.durationSeconds > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formatMediaTime(message.durationSeconds * 1000),
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    } else if (message.isFile) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { onOpenFile(message.content) }
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelf) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    // Reactions list below message content
                    if (message.reactions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .padding(top = 6.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            message.reactions.forEach { reaction ->
                                val hasReacted = reaction.fromUids.contains(repository.preferences.currentUserId)
                                Surface(
                                    onClick = { repository.reactMessage(message.mid, reaction.emoji) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (hasReacted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (hasReacted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = reaction.emoji, fontSize = 14.sp)
                                        if (reaction.count > 1) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${reaction.count}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hasReacted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Time & Status
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = (if (isSelf) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
                        )

                        if (isSelf) {
                            Spacer(modifier = Modifier.width(4.dp))
                            when (message.status) {
                                MessageStatus.SENDING -> CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 1.5.dp
                                )
                                MessageStatus.FAILED -> Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { repository.retryMessage(message) }
                                        .padding(horizontal = 2.dp, vertical = 1.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = stringResource(R.string.chat_status_failed),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = stringResource(R.string.chat_action_retry),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                MessageStatus.SUCCESS -> Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.chat_status_sent),
                                    tint = if (isSelf) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getDisplayName(context: Context, uri: Uri): String {
    var name: String? = null
    if (uri.scheme == "content") {
        context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) name = cursor.getString(index)
                }
            }
    }
    if (name.isNullOrBlank()) {
        name = uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
    }
    return name
}

/** 判断两个时间戳是否落在同一天。 */
private fun isSameDay(first: Long, second: Long): Boolean {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = first
    val firstYear = calendar.get(Calendar.YEAR)
    val firstDay = calendar.get(Calendar.DAY_OF_YEAR)
    calendar.timeInMillis = second
    return firstYear == calendar.get(Calendar.YEAR) && firstDay == calendar.get(Calendar.DAY_OF_YEAR)
}

/** 聊天列表中的日期分隔条。 */
@Composable
private fun DateSeparator(timestamp: Long) {
    val formatter = remember {
        java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault())
    }
    val label = remember(timestamp) { formatter.format(Date(timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

