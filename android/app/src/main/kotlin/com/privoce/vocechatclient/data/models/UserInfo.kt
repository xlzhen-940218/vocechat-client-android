package com.privoce.vocechatclient.data.models

import com.google.gson.annotations.SerializedName

data class UserInfo(
    val uid: Int,
    val name: String,
    val email: String? = null,
    val gender: Int? = 0,
    val language: String? = "zh-CN",
    @SerializedName("is_admin") val isAdmin: Boolean = false,
    @SerializedName("is_bot") val isBot: Boolean = false,
    @SerializedName("avatar_updated_at") val avatarUpdatedAt: Long = 0,
    @SerializedName("created_at") val createdAt: Long = 0,
    val online: Boolean = false
) {
    val displayName: String
        get() = name.ifBlank { email?.substringBefore("@") ?: "User #$uid" }

    val initials: String
        get() = displayName.take(2).uppercase()
}

