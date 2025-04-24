package com.example.dreamary.navigation

import MenuBurgerScreen
import MoreInformations
import SettingsScreen
import android.content.Context
import android.net.Uri
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.example.dreamary.views.activities.Dreams.EditDreamActivity
import com.example.dreamary.views.activities.Dreams.SuccessAddDream
import com.example.dreamary.views.activities.Guide.GuideActivity
import com.example.dreamary.views.activities.Premium.PremiumPresentation
import com.example.dreamary.views.activities.Social.ChatScreenFriendActivity
import com.example.dreamary.views.activities.profile.ProfileActivity
import com.example.dreamary.views.activities.Social.HomePageSocialActivity
import com.example.dreamary.views.activities.Social.LeaderboardScreen
import com.example.dreamary.views.activities.auth.LoginActivity
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
    NavHost(
        navController,
        startDestination = startDestination,
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {

        fun unifiedEnterTransition() = fadeIn(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            initialOffsetY = { 30 },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )

        fun unifiedExitTransition() = fadeOut(
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        ) + scaleOut(
            targetScale = 1.05f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        ) + slideOutVertically(
            targetOffsetY = { -30 },
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        )

        fun horizontalEnter() = slideInHorizontally(
            initialOffsetX = { 300 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(150))

        fun horizontalExit() = slideOutHorizontally(
            targetOffsetX = { -300 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(150))

        composable(
            route = NavRoutes.Login.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
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
            enterTransition = { horizontalEnter() },
            exitTransition = { horizontalExit() }
        ) {
            HomeActivity(navController = navController)
        }

        composable(
            route = NavRoutes.Register.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            RegisterActivity(navController = navController)
        }

        composable(
            route = NavRoutes.Profile.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            ProfileActivity(
                navController = navController,
                userId = it.arguments?.getString("userId") ?: ""
            )
        }

        composable(
            route = NavRoutes.HomeSocial.route,
            enterTransition = { horizontalEnter() },
            exitTransition = { horizontalExit() }
        ) {
            HomePageSocialActivity(navController = navController)
        }

        composable(
            route = NavRoutes.AddDream.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            AddDreamActivity(navController = navController)
        }

        composable(
            route = NavRoutes.BurgerMenu.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            MenuBurgerScreen(
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }

        composable(
            route = NavRoutes.UserMoreInformation.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            MoreInformations(navController = navController)
        }

        composable(
            route = NavRoutes.Settings.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSection = { navController.navigate(it) }
            )
        }

        composable(
            route = NavRoutes.Onboarding.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            OnboardingScreen(
                onFinish = {
                    hasSeenOnboarding.edit().putBoolean("hasSeenOnboarding", true).apply()
                    navController.navigate(NavRoutes.Login.route)
                }
            )
        }

        composable(
            route = NavRoutes.SucessAddDream.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            SuccessAddDream(navController = navController)
        }

        composable(
            route = NavRoutes.AllBadges.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            AllBadges(
                userId = it.arguments?.getString("userId") ?: "",
                navController = navController
            )
        }

        composable(
            route = NavRoutes.DreamDetail.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            DetailsDreamActivity(
                navController = navController,
                dreamId = it.arguments?.getString("dreamId") ?: "",
                userId = it.arguments?.getString("userId") ?: ""
            )
        }

        composable(
            route = NavRoutes.AllDreamsCalendar.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            AllDreamsCalendar(navController = navController)
        }

        composable(
            route = NavRoutes.ChatScreenFriends.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            ChatScreenFriendActivity(
                navController = navController,
                userId = it.arguments?.getString("userId") ?: "",
                userUrlProfilePicture = it.arguments?.getString("userUrlProfilePicture")?.let { Uri.decode(it) } ?: "",
                chatId = it.arguments?.getString("chatId") ?: ""
            )
        }

        composable(
            route = NavRoutes.EditDream.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            EditDreamActivity(
                navController = navController,
                dreamId = it.arguments?.getString("dreamId") ?: ""
            )
        }

        composable(
            route = NavRoutes.LeaderBoard.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            LeaderboardScreen(navController = navController)
        }

        composable(
            route = NavRoutes.Stats.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            com.example.dreamary.views.activities.stats.StatsScreen(navController = navController)
        }

        composable(
            route = NavRoutes.Guide.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            GuideActivity(navController)
        }

        composable(
            route = NavRoutes.Premium.route,
            enterTransition = { unifiedEnterTransition() },
            exitTransition = { unifiedExitTransition() }
        ) {
            PremiumPresentation(navController = navController)
        }
    }
}