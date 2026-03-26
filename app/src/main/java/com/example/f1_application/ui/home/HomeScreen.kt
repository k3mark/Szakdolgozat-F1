package com.example.f1_application.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.search.CircuitResultCard
import com.example.f1_application.ui.search.DriverResultCard

@Composable
fun HomeScreen(username: String, repository: F1Repository) {
    val viewModel: HomeViewModel = viewModel()
    val nextRace by viewModel.nextRace.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val favDriver by viewModel.favoriteDriver.collectAsState()
    val favTeamName by viewModel.favoriteTeamName.collectAsState()
    val favHistory by viewModel.favoriteTeamHistory.collectAsState()
    val favCircuit by viewModel.favoriteCircuit.collectAsState()
    val favTrackTime by viewModel.favoriteTrackCountdown.collectAsState()

    // JAVÍTVA: Életciklus figyelése, hogy visszatéréskor (ON_RESUME) is frissüljön az adat
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadFavoritesData(username)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "F1 DASHBOARD",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 1. KÖVETKEZŐ FUTAM SZEKCIÓ
        item {
            nextRace?.let { race ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("KÖVETKEZŐ NAGYDÍJ", style = MaterialTheme.typography.labelLarge)
                        Text(race.raceName ?: "", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Text(countdown, style = MaterialTheme.typography.displaySmall, color = Color(0xFFE10600), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }

        // 2. KEDVENC PILÓTA SZEKCIÓ
        item {
            SectionHeader("KEDVENC PILÓTÁD")
            if (favDriver != null) {
                DriverResultCard(favDriver!!)
            } else {
                Text(
                    text = "Nincs aktuális kedvenc pilótád...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // 3. KEDVENC CSAPAT SZEKCIÓ
        item {
            SectionHeader("KEDVENC CSAPATOD")
            if (favHistory.isNotEmpty()) {
                val current = favHistory.first()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = favTeamName ?: "Ismeretlen csapat",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Aktuális pontszám (2026):", style = MaterialTheme.typography.bodyLarge)
                            Text("${current.points.toInt()} PTS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Helyezés a tabellán:", style = MaterialTheme.typography.bodyLarge)
                            Text("${current.position}. hely", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = "Nincs aktuális kedvenc csapatod...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // 4. KEDVENC PÁLYA SZEKCIÓ ÉS EGYEDI VISSZASZÁMLÁLÓ
        item {
            SectionHeader("KEDVENC PÁLYÁD ÉS FUTAMOD")
            if (favCircuit != null) {
                CircuitResultCard(favCircuit!!)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HÁTRALÉVŐ IDŐ A KEDVENC FUTAMODIG:", style = MaterialTheme.typography.labelSmall)
                        Text(favTrackTime, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(
                    text = "Nincs aktuális kedvenc pályád...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary
    )
}