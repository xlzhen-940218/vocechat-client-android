package com.privoce.vocechatclient.ui.screens.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.privoce.vocechatclient.data.models.ChatMessage
import com.privoce.vocechatclient.data.models.MessageStatus
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import com.privoce.vocechatclient.ui.components.VoceAvatar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    val title = channel?.displayName ?: dmUser?.displayName ?: "Chat #$chatId"
    val subtitle = if (chatType == "channel") {
        "${channel?.members?.size ?: 0} 位成员"
    } else {
        if (dmUser?.online == true) "在线" else "离线"
    }

    val key = "${chatType}_$chatId"
    val messageList = messagesMap[key] ?: emptyList()
    val imageMessages = remember(messageList) { messageList.filter { it.isImage } }

    var inputText by remember { mutableStateOf("") }
    var replyingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }
    var previewTarget by remember { mutableStateOf<ChatMessage?>(null) }
    var showExtras by remember { mutableStateOf(false) }
    var showEmoji by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordStartAt by remember { mutableLongStateOf(0L) }
    var recordElapsed by remember { mutableIntStateOf(0) }

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
            Toast.makeText(context, "需要麦克风权限才能发送语音", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendPickedFile(uri: Uri, fallbackMime: String = "application/octet-stream") {
        val mime = context.contentResolver.getType(uri) ?: fallbackMime
        val name = getDisplayName(context, uri)
        val file = copyUriToCache(context, uri, name) ?: return
        repository.sendAttachment(chatType, chatId, file, mime, name)
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

    fun startRecording(): Boolean {
        if (!hasRecordPermission) {
            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return false
        }
        if (!voiceRecorder.start()) {
            Toast.makeText(context, "无法开始录音", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "录音失败", Toast.LENGTH_SHORT).show()
            return
        }
        if (result.durationMs < 800) {
            result.file.delete()
            Toast.makeText(context, "说话时间太短", Toast.LENGTH_SHORT).show()
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

    fun openFile(filePath: String) {
        try {
            val fileUrl = repository.apiClient.getFileUrl(filePath)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开附件: ${e.message}", Toast.LENGTH_SHORT).show()
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

    // Auto-scroll to bottom on new message or when keyboard opens
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(messageList.size, isImeVisible) {
        if (messageList.isNotEmpty()) {
            listState.animateScrollToItem(messageList.size - 1)
        }
    }

    fun sendMessage() {
        if (inputText.isBlank()) return
        val text = inputText.trim()
        val replyMid = replyingMessage?.mid
        inputText = ""
        replyingMessage = null
        repository.sendMessage(chatType, chatId, text, replyMid)
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
                    text = "消息操作",
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
                    headlineContent = { Text("回复此消息") },
                    leadingContent = { Icon(Icons.Default.Reply, contentDescription = null) },
                    modifier = Modifier.clickable {
                        replyingMessage = msg
                        selectedMessageForOptions = null
                    }
                )

                if (msg.fromUid == currentUserId) {
                    ListItem(
                        headlineContent = { Text("删除消息", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            scope.launch {
                                repository.apiClient.deleteMessage(msg.mid)
                            }
                            selectedMessageForOptions = null
                        }
                    )
                }
            }
        }
    }

    // Fullscreen image preview with zoom & paging across all chat images
    previewTarget?.let { target ->
        val isSameMessage = { msg: ChatMessage ->
            (msg.mid != 0L && msg.mid == target.mid) ||
                (msg.localMid.isNotBlank() && msg.localMid == target.localMid)
        }
        val previewImages = imageMessages.takeIf { list -> list.any(isSameMessage) } ?: listOf(target)
        val startIndex = previewImages.indexOfFirst(isSameMessage).coerceAtLeast(0)

        Dialog(
            onDismissRequest = { previewTarget = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            ImagePreviewScreen(
                images = previewImages,
                initialIndex = startIndex,
                repository = repository,
                onClose = { previewTarget = null }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
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
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                items(messageList, key = { it.localMid.ifBlank { it.mid.toString() } }) { msg ->
                    val isSelf = msg.fromUid == currentUserId
                    val sender = repository.getUser(msg.fromUid)

                    MessageBubbleItem(
                        message = msg,
                        isSelf = isSelf,
                        senderName = sender?.displayName ?: if (isSelf) "Admin" else "User #${msg.fromUid}",
                        senderAvatarUrl = repository.apiClient.getUserAvatarUrl(msg.fromUid, sender?.avatarUpdatedAt),
                        senderIsBot = sender?.isBot == true,
                        onLongClick = { selectedMessageForOptions = msg },
                        onPreviewImage = { target -> previewTarget = target },
                        onOpenFile = { path -> openFile(path) },
                        repository = repository
                    )
                }
            }

            // Replying Banner
            AnimatedVisibility(visible = replyingMessage != null) {
                replyingMessage?.let { replyMsg ->
                    val replySender = repository.getUser(replyMsg.fromUid)
                    val replySenderName = replySender?.displayName ?: "User #${replyMsg.fromUid}"
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Reply,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "回复: $replySenderName",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = replyMsg.previewText,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { replyingMessage = null }) {
                                Icon(Icons.Default.Close, contentDescription = "取消回复", modifier = Modifier.size(18.dp))
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
                            text = "正在录音 ${recordElapsed}s",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "上滑取消，松开发送",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Input Bar + attachment / emoji panels
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
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
                                showExtras = !showExtras
                                if (showExtras) {
                                    showEmoji = false
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (showExtras || showEmoji) Icons.Default.Close else Icons.Default.AddCircleOutline,
                                contentDescription = "添加附件",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("输入消息...", fontSize = 14.sp) },
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

                        IconButton(
                            onClick = {
                                showEmoji = !showEmoji
                                showExtras = showEmoji
                                if (showEmoji) keyboardController?.hide()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEmotions,
                                contentDescription = "表情",
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
                                    contentDescription = "发送",
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubbleItem(
    message: ChatMessage,
    isSelf: Boolean,
    senderName: String,
    senderAvatarUrl: String?,
    senderIsBot: Boolean = false,
    onLongClick: () -> Unit,
    onPreviewImage: (ChatMessage) -> Unit,
    onOpenFile: (String) -> Unit,
    repository: VoceChatRepository
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.createdAt) {
        timeFormat.format(Date(message.createdAt))
    }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val formattedDate = remember(message.createdAt) {
        dateFormat.format(Date(message.createdAt))
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
            if (!isSelf) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)) {
                            append(senderName)
                        }
                        append("  ")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 10.sp)) {
                            append(formattedDate)
                        }
                    },
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
                        if (message.isImage) {
                            onPreviewImage(message)
                        } else if (message.isVideo || message.isFile) {
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
                        val repliedSenderName = repliedSender?.displayName ?: "User #${repliedMsg?.fromUid ?: message.replyToMid}"
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
                                            contentDescription = "引用的图片",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { onPreviewImage(repliedMsg) }
                                        )
                                    } else if (repliedMsg.isAudio) {
                                        Text(
                                            text = repliedMsg.previewText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (repliedMsg.isVideo || repliedMsg.isFile) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { onOpenFile(repliedMsg.content) }
                                        ) {
                                            Icon(
                                                if (repliedMsg.isVideo) Icons.Default.Videocam else Icons.Default.AttachFile,
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
                                        text = "已回复消息 #${message.replyToMid}",
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
                            contentDescription = "图片",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 260.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPreviewImage(message) }
                        )
                    } else if (message.isAudio) {
                        AudioMessageBubble(
                            url = repository.apiClient.getFileUrl(message.content),
                            isSelf = isSelf,
                            durationSeconds = message.durationSeconds
                        )
                    } else if (message.isVideo) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { onOpenFile(message.content) }
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
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
                        SelectionContainer {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelf) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
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
                                MessageStatus.FAILED -> Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = "失败",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(12.dp)
                                )
                                MessageStatus.SUCCESS -> Icon(
                                    Icons.Default.Check,
                                    contentDescription = "已发送",
                                    tint = MaterialTheme.colorScheme.primary,
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

private fun copyUriToCache(context: Context, uri: Uri, displayName: String): File? {
    return try {
        val safeName = displayName.substringAfterLast('/').substringAfterLast('\\')
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}_$safeName")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file
    } catch (e: Exception) {
        Log.e("ChatScreen", "Failed to copy picked file", e)
        Toast.makeText(context, "读取文件失败", Toast.LENGTH_SHORT).show()
        null
    }
}

