package com.privoce.vocechatclient.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.privoce.vocechatclient.data.models.ChatMessage
import com.privoce.vocechatclient.data.models.GroupInfo
import com.privoce.vocechatclient.data.models.MessageStatus
import com.privoce.vocechatclient.data.models.UserInfo
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

data class UserState(
    val uid: Int,
    val online: Boolean
)

sealed class SseEvent {
    data class Ready(val ready: Boolean) : SseEvent()
    data class Chat(val message: ChatMessage) : SseEvent()
    data class GroupChanged(val group: GroupInfo) : SseEvent()
    data class RelatedGroups(val groups: List<GroupInfo>) : SseEvent()
    data class UsersSnapshot(val users: List<UserInfo>) : SseEvent()
    data class UsersState(val states: List<UserState>) : SseEvent()
    data class MessageDeleted(val mid: Long) : SseEvent()
    data class Reaction(val targetMid: Long, val fromUid: Int, val emoji: String) : SseEvent()
    object Unauthorized : SseEvent()
    object Connected : SseEvent()
    object Disconnected : SseEvent()
}

class VoceSseClient(
    private var baseUrl: String,
    private var token: String,
    private val onEvent: (SseEvent) -> Unit
) {
    private val TAG = "VoceSseClient"
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // SSE needs 0 timeout
        .build()

    private var eventSource: EventSource? = null
    private var isRunning = false
    private var retryDelaySeconds = 2L
    private var lastMid: Long = 0

    fun updateConfig(baseUrl: String, token: String) {
        this.baseUrl = baseUrl.removeSuffix("/")
        this.token = token
    }

    fun setLastMid(mid: Long) {
        if (mid > lastMid) {
            lastMid = mid
        }
    }

    fun connect() {
        if (isRunning || token.isBlank()) return
        isRunning = true
        scope.launch {
            startSse()
        }
    }

    fun disconnect() {
        isRunning = false
        eventSource?.cancel()
        eventSource = null
        onEvent(SseEvent.Disconnected)
    }

    private fun startSse() {
        if (!isRunning) return

        var url = "${baseUrl}/api/user/events?api-key=$token"
        if (lastMid > 0) {
            url += "&after_mid=$lastMid"
        }

        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/event-stream")
            .header("Referer", baseUrl)
            .build()

        val factory = EventSources.createFactory(client)
        eventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d(TAG, "SSE Connected to $baseUrl")
                retryDelaySeconds = 2L
                onEvent(SseEvent.Connected)
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data.isBlank()) return
                handleEventData(type, data)
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d(TAG, "SSE Closed")
                onEvent(SseEvent.Disconnected)
                scheduleReconnect()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.w(TAG, "SSE Failure: ${t?.message}, code: ${response?.code}")
                if (response?.code == 401) {
                    onEvent(SseEvent.Unauthorized)
                }
                onEvent(SseEvent.Disconnected)
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (!isRunning) return
        scope.launch {
            delay(retryDelaySeconds * 1000)
            retryDelaySeconds = (retryDelaySeconds * 2).coerceAtMost(30)
            if (isRunning) {
                startSse()
            }
        }
    }

    private fun handleEventData(sseType: String?, data: String) {
        try {
            val json = JsonParser.parseString(data).asJsonObject
            val eventType = json.get("type")?.asString ?: sseType ?: return

            when (eventType) {
                "ready" -> {
                    onEvent(SseEvent.Ready(true))
                }
                "chat" -> {
                    val mid = json.get("mid")?.asLong ?: return
                    setLastMid(mid)

                    val fromUid = json.get("from_uid")?.asInt ?: 0
                    val detailObj = if (json.get("detail")?.isJsonObject == true) json.getAsJsonObject("detail") else null
                    val detailType = detailObj?.get("type")?.asString

                    if (detailType == "reaction") {
                        val targetMid = detailObj.get("mid")?.asLong
                        val innerDetail = if (detailObj.get("detail")?.isJsonObject == true) detailObj.getAsJsonObject("detail") else null
                        val action = innerDetail?.get("action")?.asString
                            ?: innerDetail?.get("emoji")?.asString
                            ?: "👍"
                        if (targetMid != null) {
                            onEvent(SseEvent.Reaction(targetMid = targetMid, fromUid = fromUid, emoji = action))
                        }
                        return
                    }

                    val targetObj = if (json.get("target")?.isJsonObject == true) json.getAsJsonObject("target") else null
                    val gid = targetObj?.get("gid")?.asInt ?: json.get("gid")?.asInt
                    val dmUid = targetObj?.get("uid")?.asInt ?: json.get("target_uid")?.asInt
                    val content = detailObj?.get("content")?.asString
                        ?: json.get("content")?.asString ?: ""
                    val contentType = detailObj?.get("content_type")?.asString
                        ?: json.get("content_type")?.asString ?: "text/plain"

                    val propElement = if (detailObj?.get("properties")?.isJsonObject == true) {
                        detailObj.getAsJsonObject("properties")
                    } else if (json.get("properties")?.isJsonObject == true) {
                        json.getAsJsonObject("properties")
                    } else null

                    val properties = propElement?.let {
                        @Suppress("UNCHECKED_CAST")
                        gson.fromJson(it, object : TypeToken<Map<String, Any>>() {}.type) as? Map<String, Any>
                    }

                    val createdAt = json.get("created_at")?.asLong ?: System.currentTimeMillis()

                    val replyToMid = if (detailType == "reply") {
                        detailObj?.get("mid")?.asLong
                    } else {
                        propElement?.get("mid")?.asLong
                    }

                    val message = ChatMessage(
                        mid = mid,
                        localMid = json.get("local_mid")?.asString ?: "m_$mid",
                        fromUid = fromUid,
                        gid = gid,
                        dmUid = dmUid,
                        content = content,
                        contentType = contentType,
                        createdAt = createdAt,
                        status = MessageStatus.SUCCESS,
                        replyToMid = replyToMid,
                        properties = properties
                    )
                    onEvent(SseEvent.Chat(message))
                }
                "users_snapshot" -> {
                    val usersJson = json.getAsJsonArray("users")
                    if (usersJson != null) {
                        val type = object : TypeToken<List<UserInfo>>() {}.type
                        val users: List<UserInfo> = gson.fromJson(usersJson, type)
                        onEvent(SseEvent.UsersSnapshot(users))
                    }
                }
                "users_state" -> {
                    val usersJson = json.getAsJsonArray("users")
                    if (usersJson != null) {
                        val type = object : TypeToken<List<UserState>>() {}.type
                        val states: List<UserState> = gson.fromJson(usersJson, type)
                        onEvent(SseEvent.UsersState(states))
                    }
                }
                "related_groups" -> {
                    val groupsJson = json.getAsJsonArray("groups")
                    if (groupsJson != null) {
                        val type = object : TypeToken<List<GroupInfo>>() {}.type
                        val groups: List<GroupInfo> = gson.fromJson(groupsJson, type)
                        onEvent(SseEvent.RelatedGroups(groups))
                    }
                }
                "group_changed" -> {
                    val group = gson.fromJson(json, GroupInfo::class.java)
                    onEvent(SseEvent.GroupChanged(group))
                }
                "msg_delete" -> {
                    val mid = json.get("mid")?.asLong
                    if (mid != null) {
                        onEvent(SseEvent.MessageDeleted(mid))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse SSE event: $data", e)
        }
    }
}
