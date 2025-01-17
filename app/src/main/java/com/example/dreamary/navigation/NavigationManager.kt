package com.example.dreamary.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.views.activities.auth.LoginActivity
import com.example.dreamary.views.activities.home.HomeActivity

@Composable
fun NavigationManager() {
    val context = LocalContext.current
    val isLoggedIn = context.getSharedPreferences("isLoggedIn", Context.MODE_PRIVATE)
    val startDestination = if (isLoggedIn.getBoolean("isLoggedIn", false)) {
        NavRoutes.Home.route
    } else {
        NavRoutes.Login.route
    }

    val navController = rememberNavController()
    NavHost(navController, startDestination = startDestination) {
        composable(NavRoutes.Login.route) {
            LoginActivity(
                navController = navController,
                viewModel = viewModel()
            )
        }
        composable(NavRoutes.Home.route) {
            HomeActivity(navController = navController)
        }
    }
}