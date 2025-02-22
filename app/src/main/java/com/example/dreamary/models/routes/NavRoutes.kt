package com.example.dreamary.models.routes

sealed class NavRoutes(val route: String) {
    data object Login : NavRoutes("login")
    data object Home : NavRoutes("home")
    data object Register : NavRoutes("register")
    data object Profile : NavRoutes("profile")
    data object AddDream : NavRoutes("addDream")
    data object BurgerMenu : NavRoutes("burgerMenu")
    data object UserMoreInformation : NavRoutes("userMoreInformation")
    data object HomeSocial : NavRoutes("homeSocial")
    data object Settings : NavRoutes("settings")
    data object Onboarding : NavRoutes("onboarding")
    data object DreamDetail : NavRoutes("dreamDetail/{dreamId}") {
        fun createRoute(dreamId: String) = "dreamDetail/$dreamId"
    }
    data object EditDream : NavRoutes("editDream")
    data object SplashScreen: NavRoutes("splashScreen")
    data object SucessAddDream: NavRoutes("sucessAddDream")
    data object AllBadges : NavRoutes("allBadges")
}