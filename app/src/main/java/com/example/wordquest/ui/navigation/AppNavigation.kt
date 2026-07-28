package com.example.wordquest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wordquest.ui.screens.landing.LandingScreen
import com.example.wordquest.ui.screens.activity.ActivityScreen
import com.example.wordquest.ui.screens.settings.SettingsScreen
import com.example.wordquest.ui.screens.stats.StatsScreen

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Activity : Screen("activity")
    object Settings : Screen("settings")
    object Stats : Screen("stats")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Landing.route) {
        composable(Screen.Landing.route) { LandingScreen(navController) }
        composable(Screen.Activity.route) { ActivityScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.Stats.route) { StatsScreen(navController) }
    }
}
