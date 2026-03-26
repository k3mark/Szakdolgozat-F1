package com.example.f1_application.ui.navigation
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Standings : Screen("standings")
    object Search : Screen("search")
    object Profile : Screen("profile")
    object Compare : Screen("compare") // ÚJ
}

@Composable
fun F1NavGraph(navController: NavHostController, loggedInUser: String?, onLogin: (String) -> Unit) {
    val repository = remember { F1Repository(navController.context) }
    NavHost(navController, if (loggedInUser == null) "login" else Screen.Home.route) {
        composable("login") { LoginScreen({ onLogin(it); navController.navigate("home") }, { navController.navigate("register") }, repository) }
        composable("register") { RegisterScreen({ navController.popBackStack() }, repository) }
        composable(Screen.Home.route) { HomeScreen(loggedInUser ?: "", repository) }
        composable(Screen.Calendar.route) { CalendarScreen(loggedInUser ?: "", repository) }
        composable(Screen.Standings.route) { StandingsScreen(loggedInUser ?: "", repository) }
        composable(Screen.Search.route) { SearchScreen(repository) }
        composable(Screen.Profile.route) {
            ProfileScreen(
                username = loggedInUser ?: "",
                repository = repository,
                onLogout = { onLogin(null.toString()); navController.navigate("login") { popUpTo(0) } },
                onUsernameChanged = { onLogin(it) }
            )

        }
        composable(Screen.Compare.route) {
            CompareScreen(repository)
        }

    }
}