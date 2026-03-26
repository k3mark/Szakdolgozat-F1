package com.example.f1_application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.example.f1_application.ui.navigation.*
import com.example.f1_application.ui.theme.*
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
        containerColor = F1Dark,
        bottomBar = {
            if (loggedInUser != null && currentRoute != "login" && currentRoute != "register") {
                NavigationBar(
                    containerColor = F1Surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = F1Border,
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                    )
                ) {
                    // Profile tab removed — now in top-right avatar
                    val items = listOf(
                        Triple(Screen.Home,      "Home",      Icons.Filled.Home),
                        Triple(Screen.Calendar,  "Calendar",  Icons.Filled.DateRange),
                        Triple(Screen.Standings, "Standings", Icons.Filled.List),
                        Triple(Screen.Search,    "Search",    Icons.Filled.Search),
                        Triple(Screen.Compare,   "Compare",   Icons.Filled.Compare)
                    )
                    items.forEach { (screen, label, icon) ->
                        val selected = navBackStackEntry?.destination
                            ?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    modifier = Modifier.then(
                                        if (selected) Modifier.clip(RoundedCornerShape(4.dp))
                                        else Modifier
                                    )
                                )
                            },
                            label = {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        letterSpacing = 0.3.sp
                                    )
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = F1Red,
                                selectedTextColor = F1Red,
                                unselectedIconColor = F1TextHint,
                                unselectedTextColor = F1TextHint,
                                indicatorColor = F1Red.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.padding(padding),
            color = F1Dark
        ) {
            F1NavGraph(
                navController = navController,
                loggedInUser = loggedInUser,
                onLogin = { loggedInUser = it }
            )
        }
    }
}