package com.example.f1_application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.example.f1_application.ui.navigation.*
import com.example.f1_application.ui.theme.F1applicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { F1applicationTheme { MainScreen() } }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var loggedInUser by remember { mutableStateOf<String?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (loggedInUser != null && currentRoute != "login" && currentRoute != "register") {
                NavigationBar {
                    val items = listOf(
                        Triple(Screen.Home, "Kezdőlap", Icons.Filled.Home),
                        Triple(Screen.Calendar, "Naptár", Icons.Filled.DateRange),
                        Triple(Screen.Standings, "Állás", Icons.Filled.List),
                        Triple(Screen.Search, "Kereső", Icons.Filled.Search),
                        Triple(Screen.Compare, "Összehas.", Icons.Filled.Compare), // ÚJ
                        Triple(Screen.Profile, "Profil", Icons.Filled.Person)
                    )
                    items.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, null) }, label = { Text(label) },
                            selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = { navController.navigate(screen.route) { popUpTo(navController.graph.startDestinationId); launchSingleTop = true; restoreState = true } }
                        )
                    }
                }
            }
        }
    ) { p -> Surface(Modifier.padding(p)) { F1NavGraph(navController, loggedInUser) { loggedInUser = it } } }
}