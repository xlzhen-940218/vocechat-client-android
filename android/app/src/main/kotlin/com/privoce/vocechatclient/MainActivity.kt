package com.privoce.vocechatclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.privoce.vocechatclient.ui.navigation.VoceChatNavGraph
import com.privoce.vocechatclient.ui.theme.VoceChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as VoceChatApp).repository

        setContent {
            val isDark = when (repository.preferences.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            VoceChatTheme(
                darkTheme = isDark,
                dynamicColor = repository.preferences.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VoceChatNavGraph(repository = repository)
                }
            }
        }
    }
}

