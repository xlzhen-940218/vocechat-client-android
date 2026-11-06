package com.privoce.vocechatclient.data.models

import com.google.gson.annotations.SerializedName

enum class MessageStatus {
    SENDING,
    SUCCESS,
    FAILED
}

data class MessageReaction(
    val emoji: String,
    val count: Int,
    val fromUids: List<Int> = emptyList()
)

data class ChatMessage(
    val mid: Long = 0,
    val localMid: String = "",
    val fromUid: Int,
    val dmUid: Int? = null,
    val gid: Int? = null,
    val content: String,
    val contentType: String = "text/plain",
    val createdAt: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SUCCESS,
    val replyToMid: Long? = null,
    val replySnippet: String? = null,
    val replySenderName: String? = null,
    val reactions: List<MessageReaction> = emptyList(),
    val properties: Map<String, Any>? = null
) {
    val isChannelMessage: Boolean
        get() = gid != null && gid > 0

    val isDirectMessage: Boolean
        get() = dmUid != null && dmUid > 0

    val isImage: Boolean
        get() {
            if (contentType.startsWith("image/")) return true
            if (contentType == "vocechat/file") {
                val innerType = properties?.get("content_type") as? String
                return innerType?.startsWith("image/") == true
            }
            return false
        }

    val isAudio: Boolean
        get() = contentType.startsWith("audio/") || contentType == "vocechat/audio"

    val isFile: Boolean
        get() = !isImage && (contentType == "vocechat/file" || contentType == "application/octet-stream")

    val isMarkdown: Boolean
        get() = contentType == "text/markdown"

    val previewText: String
        get() = when {
            isImage -> "[图片]"
            isAudio -> "[语音]"
            isFile -> "[文件] ${properties?.get("name") ?: content.substringAfterLast("/")}"
            else -> content
        }
}

