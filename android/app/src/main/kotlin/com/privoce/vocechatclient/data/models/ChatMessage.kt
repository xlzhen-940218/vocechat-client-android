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

    private val innerContentType: String?
        get() = if (contentType == "vocechat/file") properties?.get("content_type")?.toString() else null

    val isImage: Boolean
        get() = contentType.startsWith("image/") || innerContentType?.startsWith("image/") == true

    val isAudio: Boolean
        get() = contentType.startsWith("audio/") ||
            contentType == "vocechat/audio" ||
            innerContentType?.startsWith("audio/") == true

    val isVideo: Boolean
        get() = contentType.startsWith("video/") || innerContentType?.startsWith("video/") == true

    val isFile: Boolean
        get() = !isImage && !isAudio && !isVideo &&
            (contentType == "vocechat/file" || contentType == "application/octet-stream")

    val isMarkdown: Boolean
        get() = contentType == "text/markdown"

    val fileName: String
        get() = properties?.get("name")?.toString() ?: content.substringAfterLast("/")

    val durationSeconds: Int
        get() = (properties?.get("duration") as? Number)?.toInt()
            ?: properties?.get("duration")?.toString()?.toIntOrNull()
            ?: 0

    val previewText: String
        get() = when {
            isImage -> "[图片]"
            isAudio -> "[语音]"
            isVideo -> "[视频] ${fileName}"
            isFile -> "[文件] $fileName"
            else -> content
        }
}

