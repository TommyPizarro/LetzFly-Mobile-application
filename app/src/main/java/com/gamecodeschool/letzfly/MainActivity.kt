package com.gamecodeschool.letzfly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gamecodeschool.letzfly.ui.Screen
import com.gamecodeschool.letzfly.ui.screens.*
import com.gamecodeschool.letzfly.ui.theme.LetzFlyTheme
import com.gamecodeschool.letzfly.ui.viewmodel.TripViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            LetzFlyTheme(darkTheme = isDarkTheme) {
                Surface {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Opening.route
                    ) {
                        composable(Screen.Opening.route) {
                            OpeningScreen(
                                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                            )
                        }

                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginSuccess = { email ->
                                    viewModel.setCurrentUser(email)
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Opening.route) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Register.route) {
                            RegisterScreen(
                                onRegisterSuccess = { email ->
                                    viewModel.setCurrentUser(email)
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Opening.route) { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToPlanTrip = { navController.navigate(Screen.PlanTrip.route) },
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                                onSelectTrip = { tripId ->
                                    navController.navigate(Screen.ManagingTrip.createRoute(tripId))
                                }
                            )
                        }

                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                viewModel = viewModel,
                                onDeleteConfirmed = {
                                    viewModel.setCurrentUser("")
                                    navController.navigate(Screen.Opening.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                isDarkTheme = isDarkTheme,
                                onThemeChanged = { isDarkTheme = it },
                                onLogout = {
                                    viewModel.setCurrentUser("")
                                    navController.navigate(Screen.Opening.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.PlanTrip.route) {
                            PlanTripScreen(
                                viewModel = viewModel,
                                onTripCreated = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.ManagingTrip.route
                        ) { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                            ManagingTripScreen(
                                tripId = tripId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}