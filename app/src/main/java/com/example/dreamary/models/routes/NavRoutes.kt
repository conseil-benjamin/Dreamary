package com.example.dreamary.models.routes

import android.net.Uri

sealed class NavRoutes(val route: String) {
    data object Login : NavRoutes("login")
    data object Home : NavRoutes("home")
    data object Register : NavRoutes("register")
    data object AddDream : NavRoutes("addDream")
    data object BurgerMenu : NavRoutes("burgerMenu")
    data object UserMoreInformation : NavRoutes("userMoreInformation")
    data object HomeSocial : NavRoutes("homeSocial")
    data object Settings : NavRoutes("settings")
    data object Onboarding : NavRoutes("onboarding")
    data object DreamDetail : NavRoutes("dreamDetail/{dreamId}") {
        fun createRoute(dreamId: String) = "dreamDetail/$dreamId"
    }
    data object Profile : NavRoutes("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    data object EditDream : NavRoutes("editDream")
    data object SplashScreen: NavRoutes("splashScreen")
    data object SucessAddDream: NavRoutes("sucessAddDream")
    data object AllBadges : NavRoutes("allBadges")
    data object AllDreamsCalendar : NavRoutes("allDreamsCalendar")
    data object ChatScreenFriends : NavRoutes("chatScreenFriend/{userId}/{userUrlProfilePicture}") {
        fun createRoute(userId: String, userUrlProfilePicture: String): String {
            val encodedUrl = Uri.encode(userUrlProfilePicture)
            return "chatScreenFriend/$userId/$encodedUrl"
        }
    }

}