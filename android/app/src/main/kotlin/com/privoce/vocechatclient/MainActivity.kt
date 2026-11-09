package com.privoce.vocechatclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.privoce.vocechatclient.ui.navigation.VoceChatNavGraph
import com.privoce.vocechatclient.ui.theme.VoceChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as VoceChatApp).repository

        setContent {
            val themeMode by repository.preferences.themeModeFlow.collectAsState()
            val dynamicColor by repository.preferences.dynamicColorFlow.collectAsState()

            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            VoceChatTheme(
                darkTheme = isDark,
                dynamicColor = dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VoceChatNavGraph(repository = repository)
                }
            }
        }
    }
}

