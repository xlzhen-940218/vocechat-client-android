package com.privoce.vocechatclient.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.privoce.vocechatclient.data.repository.VoceChatRepository
import com.privoce.vocechatclient.ui.screens.chat.ChatScreen
import com.privoce.vocechatclient.ui.screens.login.LoginScreen
import com.privoce.vocechatclient.ui.screens.main.MainScreen
import com.privoce.vocechatclient.ui.screens.server.ServerScreen

sealed class Screen(val route: String) {
    object Server : Screen("server")
    object Login : Screen("login")
    object Main : Screen("main")
    object Chat : Screen("chat/{chatType}/{id}") {
        fun createRoute(chatType: String, id: Int) = "chat/$chatType/$id"
    }
}

@Composable
fun VoceChatNavGraph(
    repository: VoceChatRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val startDestination = if (repository.preferences.isLoggedIn) {
        Screen.Main.route
    } else {
        Screen.Server.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = Screen.Server.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            ServerScreen(
                repository = repository,
                onServerConnected = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(
            route = Screen.Login.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
        ) {
            LoginScreen(
                repository = repository,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Server.route) { inclusive = true }
                    }
                },
                onChangeServer = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Main.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            MainScreen(
                repository = repository,
                onOpenChat = { chatType, id ->
                    navController.navigate(Screen.Chat.createRoute(chatType, id))
                },
                onLogout = {
                    navController.navigate(Screen.Server.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("chatType") { type = NavType.StringType },
                navArgument("id") { type = NavType.IntType }
            ),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
        ) { backStackEntry ->
            val chatType = backStackEntry.arguments?.getString("chatType") ?: "channel"
            val id = backStackEntry.arguments?.getInt("id") ?: 0

            ChatScreen(
                chatType = chatType,
                chatId = id,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

