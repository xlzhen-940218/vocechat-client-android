package com.privoce.vocechatclient.data.models

import com.google.gson.annotations.SerializedName

data class GroupInfo(
    val gid: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("is_public") val isPublic: Boolean = true,
    @SerializedName("avatar_updated_at") val avatarUpdatedAt: Long = 0,
    @SerializedName("owner_id") val ownerId: Int = 0,
    val members: List<Int>? = emptyList(),
    @SerializedName("created_at") val createdAt: Long = 0,
    @SerializedName("pinned_messages") val pinnedMessages: List<Int>? = emptyList()
) {
    val displayName: String
        get() = name.ifBlank { "Channel #$gid" }
}

