package com.wenha.cefrenglish.ui.navigation

sealed class AppRoute(val route: String) {
    data object Placement : AppRoute("placement")
    data object Lesson : AppRoute("lesson")
    data object Progress : AppRoute("progress")
}

val startDestination = AppRoute.Placement.route
