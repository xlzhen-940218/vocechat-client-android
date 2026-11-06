package com.privoce.vocechatclient.data.repository

import android.content.Context
import android.util.Log
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

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

class VoceChatRepository(context: Context) {
    private val TAG = "VoceChatRepository"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val preferences = AppPreferences(context)

    val apiClient = VoceApiClient(
        baseUrl = preferences.serverUrl,
        token = preferences.token,
        refreshToken = preferences.refreshToken,
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

    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts: StateFlow<Map<String, Int>> = _unreadCounts.asStateFlow()

    init {
        if (preferences.isLoggedIn) {
            val uid = preferences.currentUserId
            _currentUser.value = UserInfo(uid = uid, name = "User $uid")
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
        sseClient = VoceSseClient(
            baseUrl = preferences.serverUrl,
            token = token
        ) { event ->
            handleSseEvent(event)
        }
        sseClient?.connect()
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
                    val idx = list.indexOfFirst { it.mid == msg.mid }
                    if (idx >= 0) {
                        list[idx] = msg
                    } else {
                        list.add(msg)
                    }
                    currentMap[key] = list.sortedBy { it.createdAt }
                    _messages.value = currentMap
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
                val currentMap = _messages.value.toMutableMap()
                var modified = false
                currentMap.forEach { (key, list) ->
                    if (list.any { it.mid == event.mid }) {
                        currentMap[key] = list.filter { it.mid != event.mid }
                        modified = true
                    }
                }
                if (modified) {
                    _messages.value = currentMap
                }
            }
            else -> {}
        }
    }

    suspend fun checkServer(url: String): Result<ServerInfo> {
        apiClient.updateConfig(baseUrl = url)
        val result = apiClient.checkServer()
        result.onSuccess {
            preferences.serverUrl = url
            _serverInfo.value = it
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
                _users.value = userList
                val current = userList.find { it.uid == preferences.currentUserId }
                if (current != null) {
                    _currentUser.value = current
                }

                // Preload DM history for contacts so recent message shows on ChatsTab
                userList.filter { it.uid != preferences.currentUserId }.forEach { contact ->
                    loadDmHistory(contact.uid)
                }
            }

            // Load channels
            apiClient.getChannels().onSuccess { channelList ->
                _channels.value = channelList
                channelList.forEach {
                    loadChannelHistory(it.gid)
                }
            }
        }
    }

    fun loadChannelHistory(gid: Int) {
        scope.launch {
            apiClient.getChannelHistory(gid).onSuccess { fetchedMessages ->
                val key = "channel_$gid"
                val currentMap = _messages.value.toMutableMap()
                val existing = currentMap[key] ?: emptyList()
                val merged = (fetchedMessages + existing).distinctBy { it.mid }.sortedBy { it.createdAt }
                currentMap[key] = merged
                _messages.value = currentMap
            }
        }
    }

    fun loadDmHistory(dmUid: Int) {
        scope.launch {
            apiClient.getDmHistory(dmUid).onSuccess { fetchedMessages ->
                val key = "dm_$dmUid"
                val currentMap = _messages.value.toMutableMap()
                val existing = currentMap[key] ?: emptyList()
                val merged = (fetchedMessages + existing).distinctBy { it.mid }.sortedBy { it.createdAt }
                currentMap[key] = merged
                _messages.value = currentMap
            }
        }
    }

    fun sendMessage(chatType: String, id: Int, content: String, replyToMid: Long? = null) {
        scope.launch {
            val key = "${chatType}_$id"
            val localMid = "local_${System.currentTimeMillis()}"
            val pendingMsg = ChatMessage(
                mid = System.currentTimeMillis(),
                localMid = localMid,
                fromUid = preferences.currentUserId,
                gid = if (chatType == "channel") id else null,
                dmUid = if (chatType == "dm") id else null,
                content = content,
                contentType = "text/plain",
                status = MessageStatus.SENDING,
                replyToMid = replyToMid
            )

            // Optimistic update
            val currentMap = _messages.value.toMutableMap()
            val list = (currentMap[key] ?: emptyList()).toMutableList()
            list.add(pendingMsg)
            currentMap[key] = list
            _messages.value = currentMap

            // Send to server
            val sendResult = if (chatType == "channel") {
                apiClient.sendChannelMessage(id, content, replyToMid)
            } else {
                apiClient.sendDirectMessage(id, content, replyToMid)
            }

            sendResult.onSuccess { serverMid ->
                val updatedMap = _messages.value.toMutableMap()
                val updatedList = (updatedMap[key] ?: emptyList()).map {
                    if (it.localMid == localMid) {
                        it.copy(mid = serverMid, status = MessageStatus.SUCCESS)
                    } else it
                }
                updatedMap[key] = updatedList
                _messages.value = updatedMap
            }.onFailure {
                val updatedMap = _messages.value.toMutableMap()
                val updatedList = (updatedMap[key] ?: emptyList()).map {
                    if (it.localMid == localMid) {
                        it.copy(status = MessageStatus.FAILED)
                    } else it
                }
                updatedMap[key] = updatedList
                _messages.value = updatedMap
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

    fun logout() {
        sseClient?.disconnect()
        preferences.clearAuth()
        _currentUser.value = null
        _channels.value = emptyList()
        _users.value = emptyList()
        _messages.value = emptyMap()
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }

    fun getUser(uid: Int): UserInfo? {
        return _users.value.find { it.uid == uid }
    }

    fun getChannel(gid: Int): GroupInfo? {
        return _channels.value.find { it.gid == gid }
    }
}
