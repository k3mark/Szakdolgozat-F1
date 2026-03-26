package com.example.f1_application.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.auth.*
import com.example.f1_application.ui.calendar.CalendarScreen
import com.example.f1_application.ui.compare.CompareScreen
import com.example.f1_application.ui.home.HomeScreen
import com.example.f1_application.ui.profile.ProfileScreen
import com.example.f1_application.ui.standings.StandingsScreen
import com.example.f1_application.ui.search.SearchScreen

sealed class Screen(val route: String) {
    object Home      : Screen("home")
    object Calendar  : Screen("calendar")
    object Standings : Screen("standings")
    object Search    : Screen("search")
    object Profile   : Screen("profile")
    object Compare   : Screen("compare")
}

@Composable
fun F1NavGraph(
    navController: NavHostController,
    loggedInUser: String?,
    onLogin: (String) -> Unit
) {
    val repository = remember { F1Repository(navController.context) }

    NavHost(navController, if (loggedInUser == null) "login" else Screen.Home.route) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { onLogin(it); navController.navigate("home") },
                onNavigateToRegister = { navController.navigate("register") },
                repository = repository
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                repository = repository
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                username = loggedInUser ?: "",
                repository = repository,
                navController = navController
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(loggedInUser ?: "", repository)
        }
        composable(Screen.Standings.route) {
            StandingsScreen(loggedInUser ?: "", repository)
        }
        composable(Screen.Search.route) {
            SearchScreen(repository)
        }
        composable(Screen.Compare.route) {
            CompareScreen(repository)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                username = loggedInUser ?: "",
                repository = repository,
                onLogout = {
                    onLogin(null.toString())
                    navController.navigate("login") { popUpTo(0) }
                },
                onUsernameChanged = { onLogin(it) }
            )
        }
    }
}