package com.example.f1_application.ui.compare

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1_application.data.model.DriverStats
import com.example.f1_application.data.repository.F1Repository

@Composable
fun CompareScreen(repository: F1Repository) {
    val viewModel: CompareViewModel = viewModel(factory = CompareViewModelFactory(repository))

    val driverA by viewModel.driverA.collectAsState()
    val driverB by viewModel.driverB.collectAsState()
    val queryA by viewModel.queryA.collectAsState()
    val queryB by viewModel.queryB.collectAsState()
    val isLoadingA by viewModel.isLoadingA.collectAsState()
    val isLoadingB by viewModel.isLoadingB.collectAsState()
    val errorA by viewModel.errorA.collectAsState()
    val errorB by viewModel.errorB.collectAsState()
    val history by viewModel.searchHistory.collectAsState()

    // Csak pilóta típusú előzmények
    val driverHistory = history.filter { it.resultType == "DRIVER" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Pilóta összehasonlítás",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // --- KERESŐK EGYMÁS MELLETT ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DriverSearchField(
                    modifier = Modifier.weight(1f),
                    query = queryA,
                    label = "1. Pilóta",
                    isLoading = isLoadingA,
                    error = errorA,
                    onQueryChange = { viewModel.onQueryAChange(it) },
                    onSearch = { viewModel.searchDriverA() },
                    onClear = { viewModel.clearDriverA() }
                )
                DriverSearchField(
                    modifier = Modifier.weight(1f),
                    query = queryB,
                    label = "2. Pilóta",
                    isLoading = isLoadingB,
                    error = errorB,
                    onQueryChange = { viewModel.onQueryBChange(it) },
                    onSearch = { viewModel.searchDriverB() },
                    onClear = { viewModel.clearDriverB() }
                )
            }
        }

        // --- ELŐZMÉNY CHIP-EK (gyors betöltés) ---
        if (driverHistory.isNotEmpty()) {
            item {
                Text(
                    "Gyors betöltés:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(driverHistory.take(8)) { histItem ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                // Ha A üres, oda töltse, egyébként B-be
                                if (driverA == null) viewModel.searchDriverA(histItem.query)
                                else viewModel.searchDriverB(histItem.query)
                            },
                            label = { Text(histItem.query, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // --- ÖSSZEHASONLÍTÁS TÁBLÁZAT ---
        item {
            AnimatedVisibility(
                visible = driverA != null || driverB != null,
                enter = fadeIn(tween(500)) + expandVertically(tween(500)),
                exit = fadeOut() + shrinkVertically()
            ) {
                CompareTable(driverA = driverA, driverB = driverB)
            }
        }

        // --- HA MÉG NINCS SEMMI BETÖLTVE ---
        item {
            AnimatedVisibility(
                visible = driverA == null && driverB == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏎️", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Keress rá két pilótára\naz összehasonlításhoz!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- KERESŐ MEZŐ KOMPONENS ---
@Composable
fun DriverSearchField(
    modifier: Modifier = Modifier,
    query: String,
    label: String,
    isLoading: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        )
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
        Button(
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Keresés", fontSize = 13.sp)
        }
    }
}

// --- ÖSSZEHASONLÍTÁS TÁBLÁZAT ---
@Composable
fun CompareTable(driverA: DriverStats?, driverB: DriverStats?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            // FEJLÉC
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("", modifier = Modifier.weight(1.2f))
                Text(
                    driverA?.fullName?.split(" ")?.last() ?: "–",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                Text(
                    driverB?.fullName?.split(" ")?.last() ?: "–",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // SOROK
            CompareRow("Csapat", driverA?.currentTeam, driverB?.currentTeam, higherIsBetter = false)
            CompareRow("Győzelmek", driverA?.wins?.toDouble(), driverB?.wins?.toDouble())
            CompareRow("Dobogók", driverA?.podiums?.toDouble(), driverB?.podiums?.toDouble())
            CompareRow("Pole-ok", driverA?.totalPoles?.toDouble(), driverB?.totalPoles?.toDouble())
            CompareRow("Pontszám", driverA?.totalPoints, driverB?.totalPoints)
            CompareRow("Legjobb hely", driverA?.bestPosition?.toDouble(), driverB?.bestPosition?.toDouble(), higherIsBetter = false)
            CompareRow("Aktív évek", driverA?.activeYears, driverB?.activeYears, higherIsBetter = false)
        }
    }
}

// --- EGY SOR AZ ÖSSZEHASONLÍTÓBAN ---
@Composable
fun CompareRow(
    label: String,
    valueA: Any?,
    valueB: Any?,
    higherIsBetter: Boolean = true
) {
    val aDouble = (valueA as? Double)
    val bDouble = (valueB as? Double)

    // Meghatározzuk ki "nyeri" a sort
    val aWins = aDouble != null && bDouble != null &&
            if (higherIsBetter) aDouble > bDouble else aDouble < bDouble
    val bWins = aDouble != null && bDouble != null &&
            if (higherIsBetter) bDouble > aDouble else bDouble < aDouble

    // Animált háttérszín
    val aBackground by animateColorAsState(
        targetValue = if (aWins) Color(0xFF1B5E20).copy(alpha = 0.25f) else Color.Transparent,
        animationSpec = tween(600),
        label = "aBackground"
    )
    val bBackground by animateColorAsState(
        targetValue = if (bWins) Color(0xFF1B5E20).copy(alpha = 0.25f) else Color.Transparent,
        animationSpec = tween(600),
        label = "bBackground"
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sor neve
        Text(
            label,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // A érték
        Surface(
            modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
            color = aBackground,
            shape = MaterialTheme.shapes.small
        ) {
            AnimatedStatValue(
                value = valueA?.toString() ?: "–",
                highlight = aWins
            )
        }

        // B érték
        Surface(
            modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
            color = bBackground,
            shape = MaterialTheme.shapes.small
        ) {
            AnimatedStatValue(
                value = valueB?.toString() ?: "–",
                highlight = bWins
            )
        }
    }
}

// --- ANIMÁLT SZÁM ÉRTÉK ---
@Composable
fun AnimatedStatValue(value: String, highlight: Boolean) {
    val fontWeight by remember(highlight) {
        derivedStateOf { if (highlight) FontWeight.ExtraBold else FontWeight.Normal }
    }

    AnimatedContent(
        targetState = value,
        transitionSpec = {
            fadeIn(tween(300)) + slideInVertically { it / 2 } togetherWith
                    fadeOut(tween(200))
        },
        label = "statValue"
    ) { v ->
        Text(
            text = v,
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            textAlign = TextAlign.Center,
            fontWeight = fontWeight,
            color = if (highlight) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp
        )
    }
}