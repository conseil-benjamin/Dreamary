package com.example.dreamary.navigation

import MenuBurgerScreen
import SettingsScreen
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.example.dreamary.views.activities.AllDreamsCalendar.AllDreamsCalendar
import com.example.dreamary.views.activities.Dreams.AddDreamActivity
import com.example.dreamary.views.activities.Dreams.DetailsDreamActivity
import com.example.dreamary.views.activities.Dreams.SuccessAddDream
import com.example.dreamary.views.activities.profile.ProfileActivity
import com.example.dreamary.views.activities.Social.HomePageSocialActivity
import com.example.dreamary.views.activities.auth.LoginActivity
import com.example.dreamary.views.activities.auth.MoreInformations
import com.example.dreamary.views.activities.auth.RegisterActivity
import com.example.dreamary.views.activities.home.HomeActivity
import com.example.dreamary.views.activities.onboardingScreen.OnboardingScreen
import com.example.dreamary.views.activities.profile.AllBadges

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationManager() {
    val context = LocalContext.current
    val isLoggedIn = context.getSharedPreferences("isLoggedIn", Context.MODE_PRIVATE)
    val isUserInCreation = context.getSharedPreferences("userInCreation", Context.MODE_PRIVATE)
    val hasSeenOnboarding = context.getSharedPreferences("hasSeenOnboarding", Context.MODE_PRIVATE)

    Log.i("logNavigation", "hasSeenOnboarding: ${hasSeenOnboarding.getBoolean("hasSeenOnboarding", false)}")
    Log.i("logNavigation", "isLoggedIn: ${isLoggedIn.getBoolean("isLoggedIn", false)}")
    Log.i("logNavigation", "userInCreation: ${isUserInCreation.getBoolean("userInCreation", false)}")

    val startDestination = when {
        !hasSeenOnboarding.getBoolean("hasSeenOnboarding", false) -> NavRoutes.Onboarding.route
        isLoggedIn.getBoolean("isLoggedIn", false) -> NavRoutes.Home.route
        isUserInCreation.getBoolean("userInCreation", false) -> NavRoutes.UserMoreInformation.route
        else -> NavRoutes.Login.route
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
            ProfileActivity(navController = navController)
        }
        composable(NavRoutes.AddDream.route) {
            AddDreamActivity(navController = navController)
        }
        composable(NavRoutes.BurgerMenu.route) {
            MenuBurgerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSection = { navController.navigate(it) }
            )
        }
        composable(NavRoutes.UserMoreInformation.route) {
            MoreInformations(navController = navController)
        }
        composable(
            NavRoutes.HomeSocial.route,
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
            HomePageSocialActivity(navController = navController)
        }
        composable(NavRoutes.Settings.route) {
             SettingsScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToSection = { navController.navigate(it) })
        }
        composable(NavRoutes.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    hasSeenOnboarding.edit().putBoolean("hasSeenOnboarding", true).apply()
                    navController.navigate(NavRoutes.Login.route)
                }
            )
        }
        composable(NavRoutes.SucessAddDream.route){
            SuccessAddDream(
                navController = navController,
            )
        }
//        composable(NavRoutes.SplashScreen.route) {
//            SplashScreen()
//        }
        composable(NavRoutes.AllBadges.route){
            AllBadges(
                navController = navController
            )
        }
        composable(NavRoutes.DreamDetail.route){
            DetailsDreamActivity(
                navController = navController,
                dreamId = it.arguments?.getString("dreamId") ?: ""
            )
        }
        composable(NavRoutes.AllDreamsCalendar.route){
            AllDreamsCalendar(navController = navController)
        }
    }
}