package com.privoce.vocechatclient.data.repository

import android.content.Context
import android.util.Log
import com.privoce.vocechatclient.R
import com.privoce.vocechatclient.data.api.SseEvent
import com.privoce.vocechatclient.data.api.VoceApiClient
import com.privoce.vocechatclient.data.api.VoceSseClient
import com.privoce.vocechatclient.data.models.*
import com.privoce.vocechatclient.data.storage.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

class VoceChatRepository(context: Context) {
    private val TAG = "VoceChatRepository"
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val preferences = AppPreferences(context)

    val apiClient = VoceApiClient(
        baseUrl = preferences.serverUrl,
        token = preferences.token,
        refreshToken = preferences.refreshToken,
        context = appContext,
        onTokenRefreshed = { newToken, newRefresh ->
            preferences.token = newToken
            preferences.refreshToken = newRefresh
            sseClient?.updateConfig(preferences.serverUrl, newToken)
        }
    )

    private var sseClient: VoceSseClient? = null

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    private val _serverInfo = MutableStateFlow<ServerInfo?>(null)
    val serverInfo: StateFlow<ServerInfo?> = _serverInfo.asStateFlow()

    private val _channels = MutableStateFlow<List<GroupInfo>>(emptyList())
    val channels: StateFlow<List<GroupInfo>> = _channels.asStateFlow()

    private val _users = MutableStateFlow<List<UserInfo>>(emptyList())
    val users: StateFlow<List<UserInfo>> = _users.asStateFlow()

    // Key format: "channel_{gid}" or "dm_{uid}"
    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    // 未读数本地持久化，重启 App 后仍能显示
    private val _unreadCounts = MutableStateFlow(preferences.unreadCounts)
    val unreadCounts: StateFlow<Map<String, Int>> = _unreadCounts.asStateFlow()

    private var activeChatKey: String? = null

    fun setActiveChat(key: String?) {
        activeChatKey = key
        val counts = _unreadCounts.value
        if (key != null && (counts[key] ?: 0) > 0) {
            updateUnreadCounts(counts - key)
        }
    }

    private fun updateUnreadCounts(counts: Map<String, Int>) {
        _unreadCounts.value = counts
        preferences.unreadCounts = counts
    }

    init {
        if (preferences.isLoggedIn) {
            val uid = preferences.currentUserId
            _currentUser.value = UserInfo(uid = uid, name = appContext.getString(R.string.fallback_user_name, uid))
            scope.launch {
                if (!preferences.refreshToken.isNullOrBlank()) {
                    apiClient.renewToken().onSuccess { res ->
                        preferences.token = res.token
                        preferences.refreshToken = res.refreshToken
                    }
                }
                initSse()
                loadInitialData()
            }
        }
    }

    fun initSse() {
        val token = preferences.token ?: return
        sseClient?.disconnect()

        _connectionStatus.value = ConnectionStatus.CONNECTING
        // 只处理当前活跃客户端的回调，避免被替换的旧连接把状态改回“已断开”
        var newClient: VoceSseClient? = null
        newClient = VoceSseClient(
            baseUrl = preferences.serverUrl,
            token = token
        ) { event ->
            if (sseClient === newClient) handleSseEvent(event)
        }
        sseClient = newClient
        newClient.connect()
    }

    private fun handleSseEvent(event: SseEvent) {
        when (event) {
            is SseEvent.Connected -> {
                _connectionStatus.value = ConnectionStatus.CONNECTED
            }
            is SseEvent.Disconnected -> {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
            is SseEvent.Unauthorized -> {
                scope.launch {
                    apiClient.renewToken().onSuccess { res ->
                        preferences.token = res.token
                        preferences.refreshToken = res.refreshToken
                        initSse()
                    }
                }
            }
            is SseEvent.Chat -> {
                val msg = event.message
                val key = if (msg.gid != null && msg.gid > 0) {
                    "channel_${msg.gid}"
                } else if (msg.dmUid != null && msg.dmUid > 0) {
                    val otherUid = if (msg.fromUid == preferences.currentUserId) msg.dmUid else msg.fromUid
                    "dm_$otherUid"
                } else {
                    null
                }

                if (key != null) {
                    val currentMap = _messages.value.toMutableMap()
                    val list = (currentMap[key] ?: emptyList()).toMutableList()

                    // If this is the echo of a message we are still sending,
                    // replace the optimistic entry instead of appending a duplicate.
                    val incomingCid = msg.properties?.get("cid")?.toString()
                    val pendingIndex = if (!incomingCid.isNullOrBlank()) {
                        list.indexOfFirst {
                            it.localMid.isNotBlank() &&
                                it.properties?.get("cid")?.toString() == incomingCid
                        }
                    } else {
                        -1
                    }

                    if (pendingIndex >= 0) {
                        val pending = list[pendingIndex]
                        list[pendingIndex] = msg.copy(localMid = pending.localMid)
                    } else {
                        val idx = list.indexOfFirst { it.mid == msg.mid }
                        if (idx >= 0) {
                            list[idx] = msg
                        } else {
                            list.add(msg)
                        }
                    }
                    currentMap[key] = list.sortedBy { it.createdAt }
                    _messages.value = currentMap

                    if (msg.fromUid != preferences.currentUserId && key != activeChatKey) {
                        val counts = _unreadCounts.value
                        updateUnreadCounts(counts + (key to ((counts[key] ?: 0) + 1)))
                    }
                }
            }
            is SseEvent.Reaction -> {
                val currentMap = _messages.value.toMutableMap()
                var modified = false
                currentMap.forEach { (key, list) ->
                    val idx = list.indexOfFirst { it.mid == event.targetMid }
                    if (idx >= 0) {
                        val targetMsg = list[idx]
                        val reactions = targetMsg.reactions.toMutableList()
                        val rIdx = reactions.indexOfFirst { it.emoji == event.emoji }
                        if (rIdx >= 0) {
                            val r = reactions[rIdx]
                            if (!r.fromUids.contains(event.fromUid)) {
                                reactions[rIdx] = r.copy(count = r.count + 1, fromUids = r.fromUids + event.fromUid)
                            }
                        } else {
                            reactions.add(MessageReaction(emoji = event.emoji, count = 1, fromUids = listOf(event.fromUid)))
                        }
                        val updatedList = list.toMutableList()
                        updatedList[idx] = targetMsg.copy(reactions = reactions)
                        currentMap[key] = updatedList
                        modified = true
                    }
                }
                if (modified) {
                    _messages.value = currentMap
                }
            }
            is SseEvent.GroupChanged -> {
                val currentGroups = _channels.value.toMutableList()
                val index = currentGroups.indexOfFirst { it.gid == event.group.gid }
                if (index >= 0) {
                    currentGroups[index] = event.group
                } else {
                    currentGroups.add(event.group)
                }
                _channels.value = currentGroups
            }
            is SseEvent.RelatedGroups -> {
                _channels.value = event.groups
            }
            is SseEvent.UsersSnapshot -> {
                val currentUsers = _users.value.associateBy { it.uid }.toMutableMap()
                event.users.forEach { updatedUser ->
                    val old = currentUsers[updatedUser.uid]
                    currentUsers[updatedUser.uid] = if (old != null) {
                        updatedUser.copy(online = old.online)
                    } else {
                        updatedUser
                    }
                }
                _users.value = currentUsers.values.toList()
                currentUsers[preferences.currentUserId]?.let {
                    _currentUser.value = it
                }
            }
            is SseEvent.UsersState -> {
                val currentUsers = _users.value.associateBy { it.uid }.toMutableMap()
                event.states.forEach { state ->
                    val user = currentUsers[state.uid]
                    if (user != null) {
                        currentUsers[state.uid] = user.copy(online = state.online)
                    }
                }
                _users.value = currentUsers.values.toList()
            }
            is SseEvent.MessageDeleted -> {
                removeMessageById(event.mid)
            }
            else -> {}
        }
    }

    suspend fun checkServer(url: String): Result<ServerInfo> {
        val previousUrl = preferences.serverUrl
        // 规范化地址（补全协议、去掉末尾斜杠），否则 "host:port" 这类输入会直接请求失败
        val candidate = preferences.normalizeServerUrl(url)
        apiClient.updateConfig(baseUrl = candidate)
        val result = apiClient.checkServer()
        result.onSuccess {
            preferences.serverUrl = candidate
            _serverInfo.value = it
        }.onFailure {
            // Keep the previously working server if the new address is unreachable.
            if (previousUrl.isNotBlank()) {
                apiClient.updateConfig(baseUrl = previousUrl, token = preferences.token)
            }
        }
        return result
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        _connectionStatus.value = ConnectionStatus.CONNECTING
        val req = LoginRequest(
            device = "Android",
            deviceToken = "",
            credential = LoginCredential(
                type = "password",
                email = email.trim(),
                password = password
            )
        )
        val result = apiClient.login(req)
        result.onSuccess { res ->
            preferences.token = res.token
            preferences.refreshToken = res.refreshToken
            preferences.currentUserId = res.user.uid
            _currentUser.value = res.user
            apiClient.setAuthToken(res.token, res.refreshToken)

            initSse()
            loadInitialData()
        }.onFailure {
            _connectionStatus.value = ConnectionStatus.ERROR
        }
        return result
    }

    suspend fun sendLoginMagicLink(email: String): Result<String> {
        return apiClient.sendLoginMagicLink(email)
    }

    fun loadInitialData() {
        scope.launch {
            // Load server info
            apiClient.checkServer().onSuccess {
                _serverInfo.value = it
            }

            // Load users
            apiClient.getUsers().onSuccess { userList ->
                // /user 接口不返回在线状态，合并时保留 SSE 推送的在线标记
                val onlineByUid = _users.value.associate { it.uid to it.online }
                val merged = userList.map { user ->
                    if (onlineByUid[user.uid] == true) user.copy(online = true) else user
                }
                _users.value = merged
                val current = merged.find { it.uid == preferences.currentUserId }
                if (current != null) {
                    _currentUser.value = current
                }

                // Preload DM history for contacts so recent message shows on ChatsTab.
                // 只取最后一条用于列表预览，避免启动时为每个联系人拉取整段历史。
                userList.filter { it.uid != preferences.currentUserId }.forEach { contact ->
                    loadDmHistory(contact.uid, limit = 1)
                }
            }

            // Load channels
            apiClient.getChannels().onSuccess { channelList ->
                _channels.value = channelList
                channelList.forEach {
                    loadChannelHistory(it.gid, limit = 1)
                }
            }
        }
    }

    fun loadChannelHistory(gid: Int, limit: Int = 50) {
        scope.launch {
            apiClient.getChannelHistory(gid, limit = limit).onSuccess { fetchedMessages ->
                val key = "channel_$gid"
                val currentMap = _messages.value.toMutableMap()
                val existing = currentMap[key] ?: emptyList()
                val merged = (fetchedMessages + existing).distinctBy { it.mid }.sortedBy { it.createdAt }
                currentMap[key] = merged
                _messages.value = currentMap
            }
        }
    }

    fun loadDmHistory(dmUid: Int, limit: Int = 50) {
        scope.launch {
            apiClient.getDmHistory(dmUid, limit = limit).onSuccess { fetchedMessages ->
                val key = "dm_$dmUid"
                val currentMap = _messages.value.toMutableMap()
                val existing = currentMap[key] ?: emptyList()
                val merged = (fetchedMessages + existing).distinctBy { it.mid }.sortedBy { it.createdAt }
                currentMap[key] = merged
                _messages.value = currentMap
            }
        }
    }

    fun sendMessage(
        chatType: String,
        id: Int,
        content: String,
        replyToMid: Long? = null,
        mentions: List<Int> = emptyList()
    ) {
        scope.launch {
            val key = "${chatType}_$id"
            val localMid = "local_${System.currentTimeMillis()}"
            val cid = "client_${System.currentTimeMillis()}_${(0..9999).random()}"
            val pendingProps = mutableMapOf<String, Any>(
                "cid" to cid,
                // retry_text 仅用于本地重发，成功后会随服务端属性一起被替换
                "retry_text" to content
            )
            if (mentions.isNotEmpty()) pendingProps["mentions"] = mentions
            val pendingMsg = ChatMessage(
                mid = System.currentTimeMillis(),
                localMid = localMid,
                fromUid = preferences.currentUserId,
                gid = if (chatType == "channel") id else null,
                dmUid = if (chatType == "dm") id else null,
                content = content,
                contentType = "text/plain",
                status = MessageStatus.SENDING,
                replyToMid = replyToMid,
                properties = pendingProps
            )

            // Optimistic update
            val currentMap = _messages.value.toMutableMap()
            val list = (currentMap[key] ?: emptyList()).toMutableList()
            list.add(pendingMsg)
            currentMap[key] = list
            _messages.value = currentMap

            // Send to server
            val sendResult = if (chatType == "channel") {
                apiClient.sendChannelMessage(id, content, replyToMid, cid, mentions)
            } else {
                apiClient.sendDirectMessage(id, content, replyToMid, cid, mentions)
            }

            sendResult.onSuccess { serverMid ->
                finalizeMessage(key, localMid, serverMid) {
                    it.copy(mid = serverMid, status = MessageStatus.SUCCESS)
                }
            }.onFailure {
                markMessageFailed(key, localMid)
            }
        }
    }

    /**
     * Replaces the optimistic message identified by [localMid] with the final one and
     * drops any duplicate copy that may have arrived earlier through the SSE echo.
     */
    private fun finalizeMessage(
        key: String,
        localMid: String,
        serverMid: Long,
        transform: (ChatMessage) -> ChatMessage
    ) {
        val updatedMap = _messages.value.toMutableMap()
        val updatedList = (updatedMap[key] ?: emptyList())
            .filterNot { it.mid == serverMid && it.localMid != localMid }
            .map { if (it.localMid == localMid) transform(it) else it }
        updatedMap[key] = updatedList
        _messages.value = updatedMap
    }

    /**
     * Uploads a local file and sends it as a file message.
     * The server automatically fills in `name`, `content_type` and `size` properties.
     */
    fun sendAttachment(
        chatType: String,
        id: Int,
        file: File,
        mimeType: String,
        displayName: String,
        extraProperties: Map<String, Any> = emptyMap()
    ) {
        scope.launch {
            val key = "${chatType}_$id"
            val localMid = "local_${System.currentTimeMillis()}"
            val cid = "client_${System.currentTimeMillis()}_${(0..9999).random()}"
            val pendingMsg = ChatMessage(
                mid = System.currentTimeMillis(),
                localMid = localMid,
                fromUid = preferences.currentUserId,
                gid = if (chatType == "channel") id else null,
                dmUid = if (chatType == "dm") id else null,
                content = appContext.getString(R.string.state_uploading),
                contentType = "text/plain",
                status = MessageStatus.SENDING,
                // local_path / mime 仅保存在本地，用于发送失败后重试
                properties = mapOf(
                    "name" to displayName,
                    "cid" to cid,
                    "local_path" to file.absolutePath,
                    "mime" to mimeType
                ) + extraProperties
            )

            val currentMap = _messages.value.toMutableMap()
            val list = (currentMap[key] ?: emptyList()).toMutableList()
            list.add(pendingMsg)
            currentMap[key] = list
            _messages.value = currentMap

            val uploadResult = apiClient.uploadFile(file, mimeType, displayName)
            uploadResult.onFailure { e ->
                Log.e(TAG, "uploadAttachment failed", e)
                markMessageFailed(key, localMid, displayName)
            }
            uploadResult.onSuccess { path ->
                val properties = mutableMapOf<String, Any>(
                    "name" to displayName,
                    "content_type" to mimeType,
                    "size" to file.length(),
                    "cid" to cid
                )
                properties.putAll(extraProperties)

                val sendResult = if (chatType == "channel") {
                    apiClient.sendChannelFile(id, path, properties)
                } else {
                    apiClient.sendDirectFile(id, path, properties)
                }

                sendResult.onSuccess { serverMid ->
                    finalizeMessage(key, localMid, serverMid) {
                        it.copy(
                            mid = serverMid,
                            content = path,
                            contentType = "vocechat/file",
                            status = MessageStatus.SUCCESS,
                            properties = properties
                        )
                    }
                    // 发送成功后才清理缓存文件；失败时保留以便重试
                    file.delete()
                }.onFailure { e ->
                    Log.e(TAG, "sendAttachment failed", e)
                    markMessageFailed(key, localMid, displayName)
                }
            }
        }
    }

    /** 将某条本地消息标记为发送失败，并保留可用于重发的正文/文件。 */
    private fun markMessageFailed(key: String, localMid: String, displayName: String? = null) {
        val updatedMap = _messages.value.toMutableMap()
        val updatedList = (updatedMap[key] ?: emptyList()).map {
            if (it.localMid == localMid) {
                it.copy(
                    content = displayName ?: it.content,
                    status = MessageStatus.FAILED
                )
            } else it
        }
        updatedMap[key] = updatedList
        _messages.value = updatedMap
    }

    /**
     * 重发一条发送失败的消息（文本或附件）。
     * 文本使用本地保存的原始正文，附件使用仍保留在缓存中的文件。
     */
    fun retryMessage(message: ChatMessage) {
        val localMid = message.localMid
        if (localMid.isBlank() || message.status != MessageStatus.FAILED) return
        val isChannel = message.gid != null && message.gid > 0
        val id = if (isChannel) message.gid!! else message.dmUid ?: return
        val key = if (isChannel) "channel_$id" else "dm_$id"
        val props = message.properties
        val retryText = props?.get("retry_text")?.toString()
        val localPath = props?.get("local_path")?.toString()
        val mime = props?.get("mime")?.toString() ?: "application/octet-stream"
        val name = props?.get("name")?.toString() ?: message.fileName
        val cid = props?.get("cid")?.toString()
            ?: "client_${System.currentTimeMillis()}_${(0..9999).random()}"
        val extraProperties = props?.filterKeys { it == "duration" } ?: emptyMap()
        val mentions = (props?.get("mentions") as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }
            ?: emptyList()

        // 重置为发送中，文本消息恢复原始正文
        val resetMap = _messages.value.toMutableMap()
        resetMap[key] = (resetMap[key] ?: emptyList()).map {
            if (it.localMid == localMid) {
                it.copy(
                    content = retryText ?: it.content,
                    status = MessageStatus.SENDING
                )
            } else it
        }
        _messages.value = resetMap

        scope.launch {
            if (retryText != null) {
                val result = if (isChannel) {
                    apiClient.sendChannelMessage(id, retryText, message.replyToMid, cid, mentions)
                } else {
                    apiClient.sendDirectMessage(id, retryText, message.replyToMid, cid, mentions)
                }
                result.onSuccess { serverMid ->
                    finalizeMessage(key, localMid, serverMid) {
                        it.copy(mid = serverMid, status = MessageStatus.SUCCESS)
                    }
                }.onFailure {
                    markMessageFailed(key, localMid)
                }
            } else {
                val file = localPath?.let { File(it) }
                if (file == null || !file.exists()) {
                    markMessageFailed(key, localMid, name)
                    return@launch
                }
                val uploadResult = apiClient.uploadFile(file, mime, name)
                uploadResult.onFailure {
                    markMessageFailed(key, localMid, name)
                }
                uploadResult.onSuccess { path ->
                    val properties = mutableMapOf<String, Any>(
                        "name" to name,
                        "content_type" to mime,
                        "size" to file.length(),
                        "cid" to cid
                    )
                    properties.putAll(extraProperties)

                    val sendResult = if (isChannel) {
                        apiClient.sendChannelFile(id, path, properties)
                    } else {
                        apiClient.sendDirectFile(id, path, properties)
                    }
                    sendResult.onSuccess { serverMid ->
                        finalizeMessage(key, localMid, serverMid) {
                            it.copy(
                                mid = serverMid,
                                content = path,
                                contentType = "vocechat/file",
                                status = MessageStatus.SUCCESS,
                                properties = properties
                            )
                        }
                        file.delete()
                    }.onFailure {
                        markMessageFailed(key, localMid, name)
                    }
                }
            }
        }
    }

    suspend fun createChannel(name: String, description: String?, isPublic: Boolean): Result<Int> {
        val req = CreateGroupRequest(
            name = name,
            description = description,
            isPublic = isPublic
        )
        val res = apiClient.createChannel(req)
        res.onSuccess {
            loadInitialData()
        }
        return res
    }

    fun getMessage(mid: Long): ChatMessage? {
        for (list in _messages.value.values) {
            val found = list.find { it.mid == mid }
            if (found != null) return found
        }
        return null
    }

    fun reactMessage(mid: Long, emoji: String) {
        val currentUserId = preferences.currentUserId
        val currentMap = _messages.value.toMutableMap()
        currentMap.forEach { (key, list) ->
            val idx = list.indexOfFirst { it.mid == mid }
            if (idx >= 0) {
                val targetMsg = list[idx]
                val reactions = targetMsg.reactions.toMutableList()
                val rIdx = reactions.indexOfFirst { it.emoji == emoji }
                if (rIdx >= 0) {
                    val r = reactions[rIdx]
                    if (!r.fromUids.contains(currentUserId)) {
                        reactions[rIdx] = r.copy(count = r.count + 1, fromUids = r.fromUids + currentUserId)
                    }
                } else {
                    reactions.add(MessageReaction(emoji = emoji, count = 1, fromUids = listOf(currentUserId)))
                }
                val updatedList = list.toMutableList()
                updatedList[idx] = targetMsg.copy(reactions = reactions)
                currentMap[key] = updatedList
                _messages.value = currentMap
            }
        }

        scope.launch {
            apiClient.reactMessage(mid, emoji)
        }
    }

    /** 删除消息：本地立即移除，不依赖 SSE 回显（离线时也能正确反馈）。 */
    fun deleteMessage(mid: Long) {
        removeMessageById(mid)
        scope.launch {
            apiClient.deleteMessage(mid)
        }
    }

    private fun removeMessageById(mid: Long) {
        val currentMap = _messages.value.toMutableMap()
        var modified = false
        currentMap.forEach { (key, list) ->
            if (list.any { it.mid == mid }) {
                currentMap[key] = list.filter { it.mid != mid }
                modified = true
            }
        }
        if (modified) {
            _messages.value = currentMap
        }
    }

    fun logout() {
        sseClient?.disconnect()
        sseClient = null
        apiClient.clearAuth()
        preferences.clearAuth()
        _currentUser.value = null
        _channels.value = emptyList()
        _users.value = emptyList()
        _messages.value = emptyMap()
        _unreadCounts.value = emptyMap()
        preferences.unreadCounts = emptyMap()
        activeChatKey = null
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }

    fun getUser(uid: Int): UserInfo? {
        return _users.value.find { it.uid == uid }
    }

    fun getChannel(gid: Int): GroupInfo? {
        return _channels.value.find { it.gid == gid }
    }
}
