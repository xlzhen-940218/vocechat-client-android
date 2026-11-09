package com.privoce.vocechatclient.data.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vocechat_prefs", Context.MODE_PRIVATE)

    private val _themeModeFlow = MutableStateFlow(readThemeMode())
    val themeModeFlow: StateFlow<String> = _themeModeFlow.asStateFlow()

    private val _dynamicColorFlow = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    val dynamicColorFlow: StateFlow<Boolean> = _dynamicColorFlow.asStateFlow()

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "https://privoce.voce.chat") ?: "https://privoce.voce.chat"
        set(value) = prefs.edit().putString(KEY_SERVER_URL, normalizeUrl(value)).apply()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var currentUserId: Int
        get() = prefs.getInt(KEY_CURRENT_USER_ID, -1)
        set(value) = prefs.edit().putInt(KEY_CURRENT_USER_ID, value).apply()

    var themeMode: String
        get() = readThemeMode()
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value).apply()
            _themeModeFlow.value = value
        }

    var dynamicColor: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply()
            _dynamicColorFlow.value = value
        }

    private fun readThemeMode(): String =
        prefs.getString(KEY_THEME_MODE, "system") ?: "system"

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank() && currentUserId > 0

    /** 会话未读数，本地持久化；格式为 "channel_1=3\ndm_2=1"。 */
    var unreadCounts: Map<String, Int>
        get() = prefs.getString(KEY_UNREAD_COUNTS, null)
            ?.lineSequence()
            ?.mapNotNull { line ->
                val index = line.lastIndexOf('=')
                if (index <= 0) return@mapNotNull null
                val count = line.substring(index + 1).toIntOrNull() ?: return@mapNotNull null
                line.substring(0, index) to count
            }
            ?.filter { it.second > 0 }
            ?.toMap()
            ?: emptyMap()
        set(value) {
            val encoded = value.entries
                .filter { it.value > 0 }
                .joinToString("\n") { "${it.key}=${it.value}" }
            prefs.edit().putString(KEY_UNREAD_COUNTS, encoded).apply()
        }

    fun clearAuth() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_CURRENT_USER_ID)
            .remove(KEY_UNREAD_COUNTS)
            .apply()
    }

    /** 将用户输入的服务器地址规范化为带协议的 URL。 */
    fun normalizeServerUrl(url: String): String = normalizeUrl(url)

    private fun normalizeUrl(url: String): String {
        var clean = url.trim().trimEnd('/')
        if (!clean.startsWith("http://", ignoreCase = true) &&
            !clean.startsWith("https://", ignoreCase = true)
        ) {
            clean = "https://$clean"
        }
        return clean
    }

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_UNREAD_COUNTS = "unread_counts"
    }
}

