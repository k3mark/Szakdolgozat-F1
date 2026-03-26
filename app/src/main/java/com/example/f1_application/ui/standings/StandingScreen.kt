package com.example.f1_application.ui.standings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1_application.data.model.HypraceConstructorStanding
import com.example.f1_application.data.model.HypraceDriverStanding
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(username: String, repository: F1Repository) {
    val viewModel: StandingsViewModel = viewModel(factory = StandingsViewModelFactory(repository))

    val driverStandings by viewModel.driverStandings.collectAsState()
    val constructorStandings by viewModel.constructorStandings.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val viewType by viewModel.viewType.collectAsState()

    var user by remember { mutableStateOf<com.example.f1_application.data.local.UserEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val years = (2026 downTo 1950).toList()

    // Felhasználó betöltése a csillagok állapotához
    LaunchedEffect(username) {
        user = repository.getUser(username)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Bajnokság Állása",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // 1. ÉV KIVÁLASZTÁSA
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
        ) {
            OutlinedTextField(
                value = "$selectedYear Szezon",
                onValueChange = {},
                readOnly = true,
                label = { Text("Év kiválasztása") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded, { expanded = false }) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            viewModel.setYear(year)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // 2. FÜLEK (DRIVER / CONSTRUCTOR)
        TabRow(selectedTabIndex = if (viewType == StandingViewType.DRIVER) 0 else 1) {
            Tab(
                selected = viewType == StandingViewType.DRIVER,
                onClick = { viewModel.setViewType(StandingViewType.DRIVER) },
                text = { Text("Egyéni") }
            )
            Tab(
                selected = viewType == StandingViewType.CONSTRUCTOR,
                onClick = { viewModel.setViewType(StandingViewType.CONSTRUCTOR) },
                text = { Text("Csapat") }
            )
        }

        // 3. LISTA
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (viewType == StandingViewType.DRIVER) {
                items(driverStandings) { standing ->
                    val isFavorite = user?.favoriteDriverId == standing.driverId
                    StandingRow(
                        pos = standing.position ?: 0,
                        name = standing.driverName ?: "Ismeretlen",
                        points = standing.points ?: 0.0,
                        isFavorite = isFavorite,
                        onFavoriteToggle = {
                            scope.launch {
                                repository.toggleFavoriteDriver(username, standing.driverId ?: "", standing.driverName ?: "")
                                user = repository.getUser(username) // UI frissítés
                            }
                        }
                    )
                }
            } else {
                items(constructorStandings) { standing ->
                    val isFavorite = user?.favoriteTeamId == standing.teamId
                    StandingRow(
                        pos = standing.position ?: 0,
                        name = repository.toTitleCase(standing.teamName), // Kényszerített Title Case
                        points = standing.points ?: 0.0,
                        isFavorite = isFavorite,
                        onFavoriteToggle = {
                            scope.launch {
                                repository.toggleFavoriteTeam(username, standing.teamId ?: "", standing.teamName ?: "")
                                user = repository.getUser(username) // UI frissítés
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StandingRow(
    pos: Int,
    name: String,
    points: Double,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Yellow else Color.Gray
                )
            }

            Text(
                text = "$pos. $name",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${formatPointsValue(points)} PTS",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// Pont formázó segédfüggvény (pl. 25.0 -> 25)
fun formatPointsValue(points: Double): String {
    return if (points % 1.0 == 0.0) points.toInt().toString() else points.toString()
}