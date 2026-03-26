package com.example.f1_application.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1_application.data.model.CircuitStats
import com.example.f1_application.data.model.DriverStats
import com.example.f1_application.data.repository.F1Repository

@Composable
fun SearchScreen(repository: F1Repository) {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(repository))
    val searchQuery by viewModel.searchQuery.collectAsState()
    val driverResult by viewModel.driverResult.collectAsState()
    val circuitResult by viewModel.circuitResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onQueryChange(it) },
            label = { Text("Keress pilótára vagy pályára") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = { viewModel.performSearch() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Keresés")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        } else {
            if (driverResult == null && circuitResult == null && searchQuery.isNotEmpty()) {
                Text(
                    text = "Nincs találat a következőre: \"$searchQuery\"",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            driverResult?.let { DriverResultCard(it) } // JAVÍTVA
            Spacer(Modifier.height(16.dp))
            circuitResult?.let { CircuitResultCard(it) } // JAVÍTVA
        }
    }
}

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
            Text(
                text = "Aktív évek: ${driver.activeYears}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
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