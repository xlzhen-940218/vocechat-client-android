package com.privoce.vocechatclient.data.api

import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.privoce.vocechatclient.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class VoceApiClient(
    private var baseUrl: String,
    private var token: String? = null,
    private var refreshToken: String? = null,
    private val onTokenRefreshed: ((newToken: String, newRefreshToken: String) -> Unit)? = null
) {
    private val TAG = "VoceApiClient"
    private val gson = Gson()

    val okHttpClient: OkHttpClient
        get() = client

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .authenticator(object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                if (responseCount(response) >= 3) return null

                val refresh = refreshToken ?: return null
                synchronized(this) {
                    val currentReqToken = response.request.header("x-api-key")
                    if (token != null && token != currentReqToken) {
                        return response.request.newBuilder()
                            .header("x-api-key", token!!)
                            .build()
                    }

                    try {
                        val renewReq = Request.Builder()
                            .url(buildUrl("/token/renew"))
                            .header("Content-Type", "application/json")
                            .header("Referer", baseUrl)
                            .post("{\"refresh_token\":\"$refresh\"}".toRequestBody("application/json".toMediaType()))
                            .build()

                        val directClient = OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .build()

                        directClient.newCall(renewReq).execute().use { renewRes ->
                            if (renewRes.isSuccessful) {
                                val body = renewRes.body?.string() ?: ""
                                val json = JsonParser.parseString(body).asJsonObject
                                val newToken = json.get("token")?.asString
                                val newRefresh = json.get("refresh_token")?.asString
                                if (!newToken.isNullOrBlank() && !newRefresh.isNullOrBlank()) {
                                    token = newToken
                                    refreshToken = newRefresh
                                    onTokenRefreshed?.invoke(newToken, newRefresh)
                                    return response.request.newBuilder()
                                        .header("x-api-key", newToken)
                                        .build()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Token refresh failed: ${e.message}")
                    }
                }
                return null
            }

            private fun responseCount(response: Response): Int {
                var result = 1
                var prior = response.priorResponse
                while (prior != null) {
                    result++
                    prior = prior.priorResponse
                }
                return result
            }
        })
        .build()

    fun updateConfig(baseUrl: String, token: String? = null, refreshToken: String? = null) {
        this.baseUrl = baseUrl.removeSuffix("/")
        if (token != null) {
            this.token = token
        }
        if (refreshToken != null) {
            this.refreshToken = refreshToken
        }
    }

    fun setAuthToken(token: String?, refreshToken: String? = null) {
        this.token = token
        if (refreshToken != null) {
            this.refreshToken = refreshToken
        }
    }

    private fun buildUrl(path: String): String {
        return "${baseUrl.removeSuffix("/")}/api$path"
    }

    private fun newRequestBuilder(path: String): Request.Builder {
        val builder = Request.Builder()
            .url(buildUrl(path))
            .header("Referer", baseUrl)

        token?.let {
            if (it.isNotBlank()) {
                builder.header("x-api-key", it)
            }
        }
        return builder
    }

    suspend fun checkServer(): Result<ServerInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(buildUrl("/admin/system/organization"))
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val info = gson.fromJson(body, ServerInfo::class.java)
                    Result.success(info)
                } else {
                    val fallbackReq = Request.Builder()
                        .url(buildUrl("/admin/system/common"))
                        .get()
                        .build()
                    client.newCall(fallbackReq).execute().use { fbRes ->
                        if (fbRes.isSuccessful) {
                            val body = fbRes.body?.string() ?: ""
                            val info = gson.fromJson(body, ServerInfo::class.java)
                            Result.success(info)
                        } else {
                            Result.failure(IOException("Server check failed with status: ${response.code}"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(req: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(req)
            val requestBody = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(buildUrl("/token/login"))
                .header("Referer", baseUrl)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val loginRes = gson.fromJson(body, LoginResponse::class.java)
                    this@VoceApiClient.token = loginRes.token
                    this@VoceApiClient.refreshToken = loginRes.refreshToken
                    Result.success(loginRes)
                } else {
                    Result.failure(IOException("登录失败 (${response.code}): $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renewToken(): Result<LoginResponse> = withContext(Dispatchers.IO) {
        val refresh = refreshToken ?: return@withContext Result.failure(IOException("No refresh token"))
        try {
            val renewReq = Request.Builder()
                .url(buildUrl("/token/renew"))
                .header("Content-Type", "application/json")
                .header("Referer", baseUrl)
                .post("{\"refresh_token\":\"$refresh\"}".toRequestBody("application/json".toMediaType()))
                .build()

            val directClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            directClient.newCall(renewReq).execute().use { renewRes ->
                if (renewRes.isSuccessful) {
                    val body = renewRes.body?.string() ?: ""
                    val json = JsonParser.parseString(body).asJsonObject
                    val newToken = json.get("token")?.asString ?: ""
                    val newRefresh = json.get("refresh_token")?.asString ?: ""
                    token = newToken
                    refreshToken = newRefresh
                    onTokenRefreshed?.invoke(newToken, newRefresh)
                    Result.success(LoginResponse(token = newToken, refreshToken = newRefresh, user = UserInfo(uid = 0, name = "")))
                } else {
                    Result.failure(IOException("Token renew failed: ${renewRes.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendLoginMagicLink(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl("/user/send_login_magic_link?email=${Uri.encode(email.trim())}")
            val request = Request.Builder()
                .url(url)
                .header("Referer", baseUrl)
                .post("".toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("发送失败 (${response.code}): $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(): Result<List<UserInfo>> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("/user").get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "[]"
                    val type = object : TypeToken<List<UserInfo>>() {}.type
                    val list: List<UserInfo> = gson.fromJson(body, type)
                    Result.success(list)
                } else {
                    Result.failure(IOException("Failed to fetch users: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChannels(): Result<List<GroupInfo>> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("/group").get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "[]"
                    val type = object : TypeToken<List<GroupInfo>>() {}.type
                    val list: List<GroupInfo> = gson.fromJson(body, type)
                    Result.success(list)
                } else {
                    Result.failure(IOException("Failed to fetch channels: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChannelHistory(gid: Int, beforeMid: Long? = null, limit: Int = 50): Result<List<ChatMessage>> =
        withContext(Dispatchers.IO) {
            try {
                var url = buildUrl("/group/$gid/history?limit=$limit")
                if (beforeMid != null && beforeMid > 0) {
                    url += "&before=$beforeMid"
                }

                val request = Request.Builder()
                    .url(url)
                    .header("Referer", baseUrl)
                    .apply {
                        token?.let { if (it.isNotBlank()) header("x-api-key", it) }
                    }
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: "[]"
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val rawList: List<Map<String, Any>> = gson.fromJson(body, type)
                        val messages = processRawMessages(rawList, gid = gid)
                        Result.success(messages)
                    } else {
                        Result.failure(IOException("Failed to get channel history: ${response.code}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getDmHistory(dmUid: Int, beforeMid: Long? = null, limit: Int = 50): Result<List<ChatMessage>> =
        withContext(Dispatchers.IO) {
            try {
                var url = buildUrl("/user/$dmUid/history?limit=$limit")
                if (beforeMid != null && beforeMid > 0) {
                    url += "&before=$beforeMid"
                }

                val request = Request.Builder()
                    .url(url)
                    .header("Referer", baseUrl)
                    .apply {
                        token?.let { if (it.isNotBlank()) header("x-api-key", it) }
                    }
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: "[]"
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val rawList: List<Map<String, Any>> = gson.fromJson(body, type)
                        val messages = processRawMessages(rawList, dmUid = dmUid)
                        Result.success(messages)
                    } else {
                        Result.failure(IOException("Failed to get DM history: ${response.code}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun processRawMessages(
        rawList: List<Map<String, Any>>,
        gid: Int? = null,
        dmUid: Int? = null
    ): List<ChatMessage> {
        val normalMessages = mutableListOf<ChatMessage>()
        data class TempReaction(val targetMid: Long, val fromUid: Int, val emoji: String)
        val reactionItems = mutableListOf<TempReaction>()

        for (raw in rawList) {
            val detail = raw["detail"] as? Map<*, *>
            val detailType = detail?.get("type")?.toString()
            if (detailType == "reaction") {
                val targetMid = (detail["mid"] as? Number)?.toLong()
                    ?: (detail["target_mid"] as? Number)?.toLong()
                val innerDetail = detail["detail"] as? Map<*, *>
                val action = innerDetail?.get("action")?.toString()
                    ?: innerDetail?.get("emoji")?.toString()
                    ?: "👍"
                val fromUid = (raw["from_uid"] as? Number)?.toInt() ?: 0
                if (targetMid != null) {
                    reactionItems.add(TempReaction(targetMid, fromUid, action))
                }
            } else {
                mapToChatMessage(raw, gid = gid, dmUid = dmUid)?.let { normalMessages.add(it) }
            }
        }

        return normalMessages.map { msg ->
            val matching = reactionItems.filter { it.targetMid == msg.mid }
            if (matching.isNotEmpty()) {
                val grouped = matching.groupBy { it.emoji }
                val reactions = grouped.map { (emoji, list) ->
                    MessageReaction(
                        emoji = emoji,
                        count = list.size,
                        fromUids = list.map { it.fromUid }
                    )
                }
                msg.copy(reactions = reactions)
            } else {
                msg
            }
        }
    }

    suspend fun replyMessage(targetMid: Long, content: String): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                val properties = mapOf("cid" to "client_${System.currentTimeMillis()}")
                val propJson = gson.toJson(properties)
                val propBase64 = Base64.encodeToString(propJson.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

                val body = content.toByteArray(Charsets.UTF_8).toRequestBody("text/plain".toMediaType())
                val request = newRequestBuilder("/message/$targetMid/reply")
                    .header("content-type", "text/plain")
                    .header("x-properties", propBase64)
                    .post(body)
                    .build()

                Log.d("VoceApiClient", "replyMessage request to: ${request.url}")
                client.newCall(request).execute().use { response ->
                    val resBody = response.body?.string() ?: ""
                    Log.d("VoceApiClient", "replyMessage code=${response.code}, body=$resBody")
                    if (response.isSuccessful) {
                        val mid = resBody.trim().toLongOrNull() ?: System.currentTimeMillis()
                        Result.success(mid)
                    } else {
                        Result.failure(IOException("Failed to reply: ${response.code} ($resBody)"))
                    }
                }
            } catch (e: Exception) {
                Log.e("VoceApiClient", "replyMessage exception", e)
                Result.failure(e)
            }
        }

    suspend fun sendChannelMessage(gid: Int, content: String, replyToMid: Long? = null): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                if (replyToMid != null && replyToMid > 0) {
                    return@withContext replyMessage(replyToMid, content)
                }
                val properties = mapOf("cid" to "client_${System.currentTimeMillis()}")
                val propJson = gson.toJson(properties)
                val propBase64 = Base64.encodeToString(propJson.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

                val body = content.toByteArray(Charsets.UTF_8).toRequestBody("text/plain".toMediaType())
                val request = newRequestBuilder("/group/$gid/send")
                    .header("content-type", "text/plain")
                    .header("x-properties", propBase64)
                    .post(body)
                    .build()

                Log.d("VoceApiClient", "sendChannelMessage request to: ${request.url}")
                client.newCall(request).execute().use { response ->
                    val resBody = response.body?.string() ?: ""
                    Log.d("VoceApiClient", "sendChannelMessage code=${response.code}, body=$resBody")
                    if (response.isSuccessful) {
                        val mid = resBody.trim().toLongOrNull() ?: System.currentTimeMillis()
                        Result.success(mid)
                    } else {
                        Result.failure(IOException("Failed to send message: ${response.code} ($resBody)"))
                    }
                }
            } catch (e: Exception) {
                Log.e("VoceApiClient", "sendChannelMessage exception", e)
                Result.failure(e)
            }
        }

    suspend fun sendDirectMessage(dmUid: Int, content: String, replyToMid: Long? = null): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                if (replyToMid != null && replyToMid > 0) {
                    return@withContext replyMessage(replyToMid, content)
                }
                val properties = mutableMapOf<String, Any>(
                    "cid" to "client_${System.currentTimeMillis()}"
                )
                val propJson = gson.toJson(properties)
                val propBase64 = Base64.encodeToString(propJson.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

                val body = content.toByteArray(Charsets.UTF_8).toRequestBody("text/plain".toMediaType())
                val request = newRequestBuilder("/user/$dmUid/send")
                    .header("content-type", "text/plain")
                    .header("x-properties", propBase64)
                    .post(body)
                    .build()

                Log.d("VoceApiClient", "sendDirectMessage request to: ${request.url}")
                client.newCall(request).execute().use { response ->
                    val resBody = response.body?.string() ?: ""
                    Log.d("VoceApiClient", "sendDirectMessage code=${response.code}, body=$resBody")
                    if (response.isSuccessful) {
                        val mid = resBody.trim().toLongOrNull() ?: System.currentTimeMillis()
                        Result.success(mid)
                    } else {
                        Result.failure(IOException("Failed to send direct message: ${response.code} ($resBody)"))
                    }
                }
            } catch (e: Exception) {
                Log.e("VoceApiClient", "sendDirectMessage exception", e)
                Result.failure(e)
            }
        }

    suspend fun createChannel(req: CreateGroupRequest): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(req)
            val body = json.toRequestBody("application/json".toMediaType())
            val request = newRequestBuilder("/group")
                .header("content-type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val resBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val gid = resBody.trim().toIntOrNull()
                        ?: gson.fromJson(resBody, CreateGroupResponse::class.java).gid
                    Result.success(gid)
                } else {
                    Result.failure(IOException("Failed to create channel: ${response.code} $resBody"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(file: File, mimeType: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody(mimeType.toMediaType())
                )
                .build()

            val request = newRequestBuilder("/resource/file")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(body.trim().trim('"'))
                } else {
                    Result.failure(IOException("Failed to upload file: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reactMessage(mid: Long, action: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(mapOf("action" to action))
            val body = json.toRequestBody("application/json".toMediaType())
            val request = newRequestBuilder("/message/$mid/like")
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Reaction failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(mid: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("/message/$mid").delete().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Delete failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserAvatarUrl(uid: Int, avatarUpdatedAt: Long? = null): String {
        val tParam = if (avatarUpdatedAt != null && avatarUpdatedAt > 0) "&t=$avatarUpdatedAt" else ""
        val url = buildUrl("/resource/avatar?uid=$uid$tParam")
        return if (!token.isNullOrBlank()) "$url&api-key=$token" else url
    }

    fun getGroupAvatarUrl(gid: Int, avatarUpdatedAt: Long? = null): String {
        val tParam = if (avatarUpdatedAt != null && avatarUpdatedAt > 0) "&t=$avatarUpdatedAt" else ""
        val url = buildUrl("/resource/group_avatar?gid=$gid$tParam")
        return if (!token.isNullOrBlank()) "$url&api-key=$token" else url
    }

    fun getFileUrl(filePath: String): String {
        val url = buildUrl("/resource/file?file_path=${Uri.encode(filePath)}")
        return if (!token.isNullOrBlank()) "$url&api-key=$token" else url
    }

    private fun mapToChatMessage(map: Map<String, Any>, gid: Int? = null, dmUid: Int? = null): ChatMessage? {
        val mid = (map["mid"] as? Number)?.toLong() ?: return null
        val fromUid = (map["from_uid"] as? Number)?.toInt() ?: 0

        val target = map["target"] as? Map<*, *>
        val targetGid = (target?.get("gid") as? Number)?.toInt()
            ?: (map["gid"] as? Number)?.toInt()
            ?: gid
        val targetDmUid = (target?.get("uid") as? Number)?.toInt()
            ?: (map["target_uid"] as? Number)?.toInt()
            ?: dmUid

        val createdAt = (map["created_at"] as? Number)?.toLong() ?: System.currentTimeMillis()

        var content = ""
        var contentType = "text/plain"
        var properties: Map<String, Any>? = null
        var replyToMid: Long? = null

        val detail = map["detail"] as? Map<*, *>
        if (detail != null) {
            val detailType = detail["type"]?.toString()
            if (detailType == "reply") {
                replyToMid = (detail["mid"] as? Number)?.toLong()
            }
            content = detail["content"]?.toString() ?: ""
            contentType = detail["content_type"]?.toString() ?: "text/plain"
            @Suppress("UNCHECKED_CAST")
            properties = detail["properties"] as? Map<String, Any>
        } else {
            content = map["content"]?.toString() ?: ""
            contentType = map["content_type"]?.toString() ?: "text/plain"
            @Suppress("UNCHECKED_CAST")
            properties = map["properties"] as? Map<String, Any>
        }

        if (replyToMid == null) {
            replyToMid = (map["reply_to_mid"] as? Number)?.toLong()
                ?: (map["mid_reply"] as? Number)?.toLong()
                ?: ((properties?.get("mid")) as? Number)?.toLong()
        }

        return ChatMessage(
            mid = mid,
            localMid = map["local_mid"]?.toString() ?: "m_$mid",
            fromUid = fromUid,
            gid = targetGid,
            dmUid = targetDmUid,
            content = content,
            contentType = contentType,
            createdAt = createdAt,
            replyToMid = replyToMid,
            properties = properties
        )
    }
}
