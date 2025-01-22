package com.example.dreamary.navigation

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.viewmodels.auth.LoginViewModelFactory
import com.example.dreamary.views.activities.Dreams.AddDreamActivity
import com.example.dreamary.views.activities.auth.LoginActivity
import com.example.dreamary.views.activities.auth.RegisterActivity
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
        composable(
            route = NavRoutes.Login.route,
            enterTransition = {
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(200)
                ) + fadeIn(
                    initialAlpha = 0.3f,
                    animationSpec = tween(200)
                )
            },
            exitTransition = {
                scaleOut(
                    targetScale = 1.1f,
                    animationSpec = tween(200)
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            LoginActivity(
                navController = navController,
                viewModel = viewModel(factory = LoginViewModelFactory(
                    repository = AuthRepository(LocalContext.current)
                ))
            )
        }
        composable(
            route = NavRoutes.Home.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 300 },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    initialAlpha = 0.3f,
                    animationSpec = tween(150)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -300 },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(150))
            },
        ) {
            HomeActivity(navController = navController)
        }
        composable(
            route = NavRoutes.Register.route,
            enterTransition = {
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(200)
                ) + fadeIn(
                    initialAlpha = 0.3f,
                    animationSpec = tween(200)
                )
            },
            exitTransition = {
                scaleOut(
                    targetScale = 1.1f,
                    animationSpec = tween(200)
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            RegisterActivity(navController = navController)
        }
        composable(NavRoutes.Profile.route) {
            // ProfileActivity(navController = navController)
        }
        composable(NavRoutes.AddDream.route) {
            AddDreamActivity(navController = navController)
        }
    }
}