package com.example.f1_application.ui.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1_application.data.model.CircuitStats
import com.example.f1_application.data.model.DriverStats
import com.example.f1_application.data.repository.F1Repository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SearchScreen(repository: F1Repository) {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(repository))
    val searchQuery by viewModel.searchQuery.collectAsState()
    val driverResult by viewModel.driverResult.collectAsState()
    val circuitResult by viewModel.circuitResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    // Eredmény látható-e?
    val hasResult = driverResult != null || circuitResult != null

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- KERESŐ MEZŐ ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onQueryChange(it) },
            label = { Text("Keress pilótára vagy pályára") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            }
        )

        Button(
            onClick = { viewModel.performSearch() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Keresés")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // --- BETÖLTÉS ---
            item {
                AnimatedVisibility(visible = isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                    )
                }
            }

            // --- NEM TALÁLHATÓ ---
            item {
                AnimatedVisibility(
                    visible = !isLoading && !hasResult && searchQuery.isNotEmpty(),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Nincs találat: \"$searchQuery\"",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // --- PILÓTA EREDMÉNY ---
            item {
                AnimatedVisibility(
                    visible = driverResult != null,
                    enter = fadeIn(tween(400)) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(400)
                    ),
                    exit = fadeOut()
                ) {
                    driverResult?.let { DriverResultCard(it) }
                }
            }

            // --- PÁLYA EREDMÉNY ---
            item {
                AnimatedVisibility(
                    visible = circuitResult != null,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(500, delayMillis = 100)
                    ),
                    exit = fadeOut()
                ) {
                    circuitResult?.let { CircuitResultCard(it) }
                }
            }

            // --- ELŐZMÉNY SZEKCIÓ FEJLÉC ---
            if (searchHistory.isNotEmpty() && !hasResult) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Korábbi keresések",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Összes törlése", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // --- ELŐZMÉNY ELEMEK animálva ---
                itemsIndexed(searchHistory) { index, item ->
                    AnimatedHistoryItem(
                        item = item,
                        index = index,
                        onClick = { viewModel.performSearch(item.query) },
                        onDelete = { viewModel.deleteHistoryItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedHistoryItem(
    item: com.example.f1_application.data.local.SearchHistoryEntity,
    index: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L) // Soronként késleltetett megjelenés
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = tween(300)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = when (item.resultType) {
                    "DRIVER" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    "CIRCUIT" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.query, fontWeight = FontWeight.Medium)
                    Text(
                        text = when (item.resultType) {
                            "DRIVER" -> "Pilóta"
                            "CIRCUIT" -> "Pálya"
                            else -> "Nincs találat"
                        } + " • " + formatTimestamp(item.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Törlés",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// --- RESULT CARD-OK (változatlan marad) ---
@Composable
fun DriverResultCard(driver: DriverStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(driver.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Csapat: ${driver.currentTeam}", style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Győzelmek: ${driver.wins}")
                    Text("Dobogók: ${driver.podiums}")
                    Text("Pole-ok: ${driver.totalPoles}")
                }
                Column {
                    Text("Pontszám: ${driver.totalPoints}")
                    Text("Legjobb hely: ${driver.bestPosition}.")
                }
            }
            Text("Aktív évek: ${driver.activeYears}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun CircuitResultCard(circuit: CircuitStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(circuit.circuitName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pályahossz", style = MaterialTheme.typography.labelMedium)
                    Text(circuit.trackLength ?: "Nincs adat", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Körök", style = MaterialTheme.typography.labelMedium)
                    Text("${circuit.lapCount ?: "?"}", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Versenytáv", style = MaterialTheme.typography.labelMedium)
                    Text(circuit.totalDistance ?: "Nincs adat", fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Utolsó 5 év Pole-pozíciói:", fontWeight = FontWeight.Bold)
            circuit.lastFivePoles.forEach { pole ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${pole.year}: ${pole.driverName}")
                    Text(pole.poleTime ?: "", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}