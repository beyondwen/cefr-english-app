package com.wenha.cefrenglish.ui.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Placement : AppRoute("placement")
    data object Lesson : AppRoute("lesson")
    data object Progress : AppRoute("progress")
}

val startDestination = AppRoute.Home.route
