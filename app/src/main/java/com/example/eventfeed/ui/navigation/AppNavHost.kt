package com.example.eventfeed.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.eventfeed.ui.events.detail.EventDetailScreen
import com.example.eventfeed.ui.events.list.EventListScreen
import com.example.eventfeed.ui.login.LoginScreen
import com.example.eventfeed.ui.profile.UserProfileScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {

        composable("login") { LoginScreen(onLoginSuccess = { navController.navigate("events") }) }

        composable("events") {
            EventListScreen(
                onOpen = { id -> navController.navigate("detail/$id") },
                onProfileClicked = { navController.navigate("profile") })
        }

        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            EventDetailScreen(
                eventId = id,
                onBack = { navController.navigate("events") },
                onProfileClicked = {
                    navController.navigate("profile")
                })
        }

        composable("profile") { UserProfileScreen(onBack = { navController.navigateUp() }) }
    }
}