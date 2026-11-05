package com.gamecodeschool.letzfly.ui

sealed class Screen(val route: String) {
    object Opening : Screen("opening")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object PlanTrip : Screen("plan_trip")
    object ManagingTrip : Screen("managing_trip/{tripId}") {
        fun createRoute(tripId: String) = "managing_trip/$tripId"
    }
}