package com.privoce.vocechatclient.data.models

import com.google.gson.annotations.SerializedName

data class LoginCredential(
    val type: String = "password",
    val email: String,
    val password: String
)

data class LoginRequest(
    val device: String = "Android",
    @SerializedName("device_token") val deviceToken: String = "",
    val credential: LoginCredential
)

data class LoginResponse(
    @SerializedName("server_id") val serverId: String? = null,
    val token: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    val user: UserInfo,
    @SerializedName("expired_in") val expiredIn: Long? = null
)

data class TokenRenewRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class TokenRenewResponse(
    val token: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expired_in") val expiredIn: Long? = null
)

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    @SerializedName("is_public") val isPublic: Boolean = true,
    val members: List<Int> = emptyList()
)

data class CreateGroupResponse(
    val gid: Int
)
