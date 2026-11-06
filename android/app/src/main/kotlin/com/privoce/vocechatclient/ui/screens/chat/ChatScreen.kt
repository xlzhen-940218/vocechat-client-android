package com.privoce.vocechatclient.ui.screens.chat

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.privoce.vocechatclient.data.models.ChatMessage
import com.privoce.vocechatclient.data.models.MessageStatus
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import com.privoce.vocechatclient.ui.components.VoceAvatar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    var inputText by remember { mutableStateOf("") }
    var replyingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }

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
                        senderName = sender?.displayName ?: "User #${msg.fromUid}",
                        senderAvatarUrl = repository.apiClient.getUserAvatarUrl(msg.fromUid, sender?.avatarUpdatedAt),
                        senderIsBot = sender?.isBot == true,
                        onLongClick = { selectedMessageForOptions = msg },
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

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { /* File/Image Attachment */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
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
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { sendMessage() },
                        enabled = inputText.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubbleItem(
    message: ChatMessage,
    isSelf: Boolean,
    senderName: String,
    senderAvatarUrl: String?,
    senderIsBot: Boolean = false,
    onLongClick: () -> Unit,
    repository: VoceChatRepository
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val formattedDate = remember(message.createdAt) {
        dateFormat.format(Date(message.createdAt))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLongClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        VoceAvatar(
            avatarUrl = senderAvatarUrl,
            name = senderName,
            size = 38.dp,
            isBot = senderIsBot,
            modifier = Modifier.padding(top = 2.dp, end = 12.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Header: Name + Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )

                if (isSelf) {
                    Spacer(modifier = Modifier.width(6.dp))
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
                        MessageStatus.SUCCESS -> {}
                    }
                }
            }

            // Replying Quote Card ("包含关系")
            if (message.replyToMid != null && message.replyToMid > 0) {
                val repliedMsg = repository.getMessage(message.replyToMid)
                val repliedSender = repliedMsg?.let { repository.getUser(it.fromUid) }
                val repliedSenderName = repliedSender?.displayName ?: "User #${repliedMsg?.fromUid ?: message.replyToMid}"
                val repliedAvatarUrl = repliedSender?.let { repository.apiClient.getUserAvatarUrl(it.uid, it.avatarUpdatedAt) }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
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
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (repliedMsg != null) {
                            if (repliedMsg.isImage) {
                                val imgUrl = repository.apiClient.getFileUrl(repliedMsg.content)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 100.dp, max = 200.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imgUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "引用的图片",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else if (repliedMsg.isFile) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = repliedMsg.properties?.get("name")?.toString() ?: repliedMsg.content.substringAfterLast("/"),
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
                val imageUrl = repository.apiClient.getFileUrl(message.content)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 280.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .listener(
                                onError = { _, result ->
                                    Log.e("ChatScreen", "AsyncImage FAILED for $imageUrl: ${result.throwable.message}", result.throwable)
                                },
                                onSuccess = { _, _ ->
                                    Log.d("ChatScreen", "AsyncImage SUCCESS for $imageUrl")
                                }
                            )
                            .build(),
                        contentDescription = "图片",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (message.isFile) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.properties?.get("name")?.toString() ?: message.content.substringAfterLast("/"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                SelectionContainer {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 1.dp)
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
        }
    }
}

