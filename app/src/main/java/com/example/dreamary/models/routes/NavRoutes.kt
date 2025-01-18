package com.example.dreamary.models.routes

sealed class NavRoutes(val route: String) {
    data object Login : NavRoutes("login")
    data object Home : NavRoutes("home")
    data object Register : NavRoutes("register")
}