package com.example.f1_application.ui.calendar

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
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(username: String, repository: F1Repository) {
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModelFactory(repository))
    val races by viewModel.races.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    var user by remember { mutableStateOf<com.example.f1_application.data.local.UserEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val years = (2026 downTo 1950).toList()

    LaunchedEffect(username) {
        user = repository.getUser(username)
    }

    Column(Modifier.fillMaxSize()) {
        Text("Versenynaptár", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))

        ExposedDropdownMenuBox(expanded, { expanded = !expanded }, Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
            OutlinedTextField("$selectedYear Szezon", {}, readOnly = true, label = { Text("Szezon") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded, { expanded = false }) {
                years.forEach { y -> DropdownMenuItem(text = { Text(y.toString()) }, onClick = { viewModel.updateYear(y); expanded = false }) }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(races) { race ->
                val isFav = race.circuitId != null && user?.favoriteTrackId == race.circuitId

                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (race.circuitId != null) {
                                scope.launch {
                                    // Most már átadjuk mindkét nevet a kombinált mentéshez!
                                    repository.toggleFavoriteTrack(
                                        username = username,
                                        trackId = race.circuitId!!,
                                        gpName = race.raceName ?: "Ismeretlen Nagydíj",
                                        circuitName = race.officialName ?: "Ismeretlen pálya"
                                    )
                                    user = repository.getUser(username)
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (isFav) Color.Yellow else Color.Gray
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            // NAGYDÍJ NEVE (pl. Hungarian Grand Prix)
                            Text(race.raceName ?: "", fontWeight = FontWeight.Bold)

                            // PÁLYA NEVE (pl. Hungaroring) - officialName-ben tároljuk
                            Text(
                                text = race.officialName ?: "Ismeretlen pálya",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.height(4.dp))
                            Text("Dátum: ${formatF1Date(race.startDate ?: "")}", style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            text = if(race.status == "Finished") "Befejezve" else "Hátravan",
                            style = MaterialTheme.typography.labelSmall,
                            color = if(race.status == "Finished") Color.Gray else MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun formatF1Date(rawDate: String): String = if (rawDate.length >= 10) rawDate.substring(0, 10).replace("-", ". ") else rawDate