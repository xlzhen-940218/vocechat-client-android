package com.privoce.vocechatclient.data.storage

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vocechat_prefs", Context.MODE_PRIVATE)

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
        get() = prefs.getString(KEY_THEME_MODE, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var dynamicColor: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        set(value) = prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank() && currentUserId > 0

    fun clearAuth() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_CURRENT_USER_ID)
            .apply()
    }

    private fun normalizeUrl(url: String): String {
        var clean = url.trim().removeSuffix("/")
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
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
    }
}

