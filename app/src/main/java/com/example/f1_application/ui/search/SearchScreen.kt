package com.example.f1_application.ui.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.f1_application.data.model.CircuitStats
import com.example.f1_application.data.model.DriverStats
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.common.*
import com.example.f1_application.ui.navigation.Screen
import com.example.f1_application.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SearchScreen(repository: F1Repository, username: String, navController: NavController) {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(repository))
    val searchQuery by viewModel.searchQuery.collectAsState()
    val driverResult by viewModel.driverResult.collectAsState()
    val circuitResult by viewModel.circuitResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val hasResult = driverResult != null || circuitResult != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("SEARCH", style = MaterialTheme.typography.headlineLarge, color = F1Red)
                Text("DRIVER & CIRCUIT DATA", style = MaterialTheme.typography.labelLarge, color = F1TextHint, letterSpacing = 3.sp)
            }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(F1Surface)
                    .border(2.dp, F1Red, CircleShape)
                    .clickable { navController.navigate(Screen.Profile.route) },
                contentAlignment = Alignment.Center
            ) {
                Text(username.take(2).uppercase(), style = MaterialTheme.typography.labelLarge, color = F1Red, fontWeight = FontWeight.Black)
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onQueryChange(it) },
            placeholder = { Text("e.g. Verstappen, Hungaroring...", color = F1TextHint) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = F1Red, unfocusedBorderColor = F1Border,
                focusedTextColor = F1TextPrim, unfocusedTextColor = F1TextPrim,
                cursorColor = F1Red, focusedContainerColor = F1Surface, unfocusedContainerColor = F1Surface
            ),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = F1TextHint) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Clear, null, tint = F1TextHint)
                    }
                }
            },
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.performSearch() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = F1Red, contentColor = F1TextPrim),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("SEARCH", style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                AnimatedVisibility(visible = isLoading) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(color = F1Red, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    }
                }
            }
            item {
                AnimatedVisibility(visible = !isLoading && !hasResult && searchQuery.isNotEmpty(), enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(F1Surface).border(1.dp, F1Red.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(16.dp)
                    ) {
                        Text("No results for: \"$searchQuery\"", color = F1Red, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                AnimatedVisibility(visible = driverResult != null, enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(400)), exit = fadeOut()) {
                    driverResult?.let { DriverResultCard(it) }
                }
            }
            item {
                AnimatedVisibility(visible = circuitResult != null, enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500, delayMillis = 100)), exit = fadeOut()) {
                    circuitResult?.let { CircuitResultCard(it) }
                }
            }
            if (searchHistory.isNotEmpty() && !hasResult) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("RECENT SEARCHES", style = MaterialTheme.typography.labelLarge, color = F1TextHint)
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Clear all", color = F1Red, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                itemsIndexed(searchHistory) { index, item ->
                    AnimatedHistoryItem(item = item, index = index, onClick = { viewModel.performSearch(item.query) }, onDelete = { viewModel.deleteHistoryItem(item.id) })
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
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300))) {
        val accentColor = when (item.resultType) {
            "DRIVER" -> F1Red
            "CIRCUIT" -> F1Gold
            else -> F1TextHint
        }
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(F1Surface)
                .border(1.dp, F1Border, RoundedCornerShape(8.dp)).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(accentColor))
            Spacer(Modifier.width(12.dp))
            Icon(Icons.Default.History, contentDescription = null, tint = F1TextHint, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(item.query, color = F1TextPrim, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    text = when (item.resultType) {
                        "DRIVER" -> "DRIVER"
                        "CIRCUIT" -> "CIRCUIT"
                        else -> "NO RESULT"
                    } + "  ·  " + formatTimestamp(item.timestamp),
                    style = MaterialTheme.typography.labelSmall, color = F1TextHint
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = F1TextHint, modifier = Modifier.size(16.dp))
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun DriverResultCard(driver: DriverStats) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(F1Surface).border(1.dp, F1Border, RoundedCornerShape(8.dp))) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(F1Red))
        Column(Modifier.padding(start = 16.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)) {
            Text(driver.fullName.uppercase(), style = MaterialTheme.typography.headlineSmall, color = F1TextPrim, fontWeight = FontWeight.Black)
            Text(driver.currentTeam, style = MaterialTheme.typography.bodySmall, color = F1Red, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            F1Divider()
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                F1StatCell("WINS", "${driver.wins}", F1Red, Modifier.weight(1f))
                F1StatCell("PODIUMS", "${driver.podiums}", F1Gold, Modifier.weight(1f))
                F1StatCell("POLES", "${driver.totalPoles}", F1Orange, Modifier.weight(1f))
                F1StatCell("POINTS", "${driver.totalPoints.toInt()}", F1TextPrim, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Best position: ${driver.bestPosition}.", style = MaterialTheme.typography.bodySmall, color = F1TextSec)
                Text(driver.activeYears, style = MaterialTheme.typography.bodySmall, color = F1TextHint, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun CircuitResultCard(circuit: CircuitStats) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(F1Surface).border(1.dp, F1Border, RoundedCornerShape(8.dp))) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(F1Gold))
        Column(Modifier.padding(start = 16.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)) {
            Text(circuit.circuitName.uppercase(), style = MaterialTheme.typography.headlineSmall, color = F1TextPrim, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            F1Divider()
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                F1StatCell("LENGTH", circuit.trackLength ?: "–", F1Gold, Modifier.weight(1f))
                F1StatCell("LAPS", "${circuit.lapCount ?: "–"}", F1Red, Modifier.weight(1f))
                F1StatCell("DISTANCE", circuit.totalDistance ?: "–", F1Orange, Modifier.weight(1f))
            }
            if (circuit.lastFivePoles.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                F1Divider()
                Spacer(Modifier.height(10.dp))
                Text("POLE POSITIONS", style = MaterialTheme.typography.labelLarge, color = F1TextHint)
                Spacer(Modifier.height(6.dp))
                circuit.lastFivePoles.forEach { pole ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(F1Red))
                            Spacer(Modifier.width(8.dp))
                            Text("${pole.year}  ${pole.driverName}", color = F1TextPrim, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(pole.poleTime ?: "", style = MaterialTheme.typography.bodySmall, color = F1Gold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun F1StatCell(label: String, value: String, valueColor: Color = F1Red, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = F1TextHint)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, fontSize = 16.sp), color = valueColor)
    }
}

@Composable
fun F1Divider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = F1Border)
}