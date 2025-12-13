package com.example.eventfeed.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.eventfeed.ui.events.EventDetailScreen
import com.example.eventfeed.ui.events.EventListScreen
import com.example.eventfeed.ui.login.LoginScreen
import com.example.eventfeed.ui.profile.UserProfileScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {

        composable("login") { LoginScreen(onLogin = { navController.navigate("events") }) }

        composable("events") { EventListScreen(onOpen = { id -> navController.navigate("detail/$id") }) }

        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            EventDetailScreen(eventId = id)
        }

        composable("profile") { UserProfileScreen() }
    }
}