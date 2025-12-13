package com.example.eventfeed.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.eventfeed.ui.navigation.AppNavHost
import com.example.eventfeed.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot() {
    AppTheme {
        val navController = rememberNavController()
        AppNavHost(navController = navController)
    }
}