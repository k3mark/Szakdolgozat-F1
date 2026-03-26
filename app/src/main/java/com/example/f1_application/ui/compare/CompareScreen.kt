package com.example.f1_application.ui.compare

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1_application.data.model.DriverStats
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.theme.*

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
    val driverHistory = history.filter { it.resultType == "DRIVER" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── FEJLÉC ───────────────────────────────────────────────
        item {
            Text(
                text = "ÖSSZEHASONLÍTÁS",
                style = MaterialTheme.typography.headlineLarge,
                color = F1Red
            )
            Text(
                text = "DRIVER HEAD-TO-HEAD",
                style = MaterialTheme.typography.labelLarge,
                color = F1TextHint,
                letterSpacing = 3.sp
            )
        }

        // ── KERESŐK ──────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DriverSearchField(
                    modifier = Modifier.weight(1f),
                    query = queryA,
                    label = "1. PILÓTA",
                    accentColor = F1Red,
                    isLoading = isLoadingA,
                    error = errorA,
                    onQueryChange = { viewModel.onQueryAChange(it) },
                    onSearch = { viewModel.searchDriverA() },
                    onClear = { viewModel.clearDriverA() }
                )
                DriverSearchField(
                    modifier = Modifier.weight(1f),
                    query = queryB,
                    label = "2. PILÓTA",
                    accentColor = F1Gold,
                    isLoading = isLoadingB,
                    error = errorB,
                    onQueryChange = { viewModel.onQueryBChange(it) },
                    onSearch = { viewModel.searchDriverB() },
                    onClear = { viewModel.clearDriverB() }
                )
            }
        }

        // ── ELŐZMÉNY CHIP-EK ─────────────────────────────────────
        if (driverHistory.isNotEmpty()) {
            item {
                Text(
                    text = "GYORS BETÖLTÉS",
                    style = MaterialTheme.typography.labelLarge,
                    color = F1TextHint,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(driverHistory.take(8)) { histItem ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(F1Surface)
                                .border(1.dp, F1Border, RoundedCornerShape(4.dp))
                                .clickable {
                                    if (driverA == null) viewModel.searchDriverA(histItem.query)
                                    else viewModel.searchDriverB(histItem.query)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = histItem.query,
                                style = MaterialTheme.typography.labelMedium,
                                color = F1TextSec
                            )
                        }
                    }
                }
            }
        }

        // ── ÖSSZEHASONLÍTÁS TÁBLA ─────────────────────────────────
        item {
            AnimatedVisibility(
                visible = driverA != null || driverB != null,
                enter = fadeIn(tween(500)) + expandVertically(tween(500)),
                exit = fadeOut() + shrinkVertically()
            ) {
                CompareTable(driverA = driverA, driverB = driverB)
            }
        }

        // ── ÜRES ÁLLAPOT ──────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = driverA == null && driverB == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(F1Surface)
                        .border(1.dp, F1Border, RoundedCornerShape(8.dp))
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏎", fontSize = 36.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Keress rá két pilótára\naz összehasonlításhoz!",
                            textAlign = TextAlign.Center,
                            color = F1TextHint,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

// ── KERESŐ MEZŐ ─────────────────────────────────────────────────────
@Composable
fun DriverSearchField(
    modifier: Modifier = Modifier,
    query: String,
    label: String,
    accentColor: Color = F1Red,
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
            placeholder = { Text(label, color = F1TextHint, style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = F1Border,
                focusedTextColor = F1TextPrim,
                unfocusedTextColor = F1TextPrim,
                cursorColor = accentColor,
                focusedContainerColor = F1Surface,
                unfocusedContainerColor = F1Surface,
                errorBorderColor = F1Red,
                errorContainerColor = F1Surface
            ),
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = accentColor
                    )
                } else if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, null, tint = F1TextHint, modifier = Modifier.size(16.dp))
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = F1TextPrim)
        )
        error?.let {
            Text(it, color = F1Red, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
        }
        Button(
            onClick = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = F1TextPrim
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("KERES", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ── ÖSSZEHASONLÍTÁS TÁBLA ────────────────────────────────────────────
@Composable
fun CompareTable(driverA: DriverStats?, driverB: DriverStats?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(F1Surface)
            .border(1.dp, F1Border, RoundedCornerShape(8.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            // Fejléc
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1.3f))
                Text(
                    text = driverA?.fullName?.split(" ")?.last()?.uppercase() ?: "–",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = F1Red,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = driverB?.fullName?.split(" ")?.last()?.uppercase() ?: "–",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = F1Gold,
                    fontWeight = FontWeight.Black
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = F1Border)

            // Sorok
            CompareRow("CSAPAT",
                driverA?.currentTeam, driverB?.currentTeam, higherIsBetter = false)
            CompareRow("GYŐZELMEK",
                driverA?.wins?.toDouble(), driverB?.wins?.toDouble())
            CompareRow("DOBOGÓK",
                driverA?.podiums?.toDouble(), driverB?.podiums?.toDouble())
            CompareRow("POLE-OK",
                driverA?.totalPoles?.toDouble(), driverB?.totalPoles?.toDouble())
            CompareRow("PONTOK",
                driverA?.totalPoints, driverB?.totalPoints)
            CompareRow("LEGJOBB HELY",
                driverA?.bestPosition?.toDouble(), driverB?.bestPosition?.toDouble(), higherIsBetter = false)
            CompareRow("AKTÍV ÉVEK",
                driverA?.activeYears, driverB?.activeYears, higherIsBetter = false)
        }
    }
}

// ── EGY ÖSSZEHASONLÍTÁS SOR ──────────────────────────────────────────
@Composable
fun CompareRow(
    label: String,
    valueA: Any?,
    valueB: Any?,
    higherIsBetter: Boolean = true
) {
    val aDouble = valueA as? Double
    val bDouble = valueB as? Double
    val aWins = aDouble != null && bDouble != null &&
            if (higherIsBetter) aDouble > bDouble else aDouble < bDouble
    val bWins = aDouble != null && bDouble != null &&
            if (higherIsBetter) bDouble > aDouble else bDouble < aDouble

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.3f),
            style = MaterialTheme.typography.labelSmall,
            color = F1TextHint
        )

        // A érték
        AnimatedCompareCell(
            value = valueA?.toString() ?: "–",
            wins = aWins,
            winColor = F1Red,
            modifier = Modifier.weight(1f)
        )

        // B érték
        AnimatedCompareCell(
            value = valueB?.toString() ?: "–",
            wins = bWins,
            winColor = F1Gold,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── ANIMÁLT CELLA (counter animáció + kiemelés) ──────────────────────
@Composable
fun AnimatedCompareCell(
    value: String,
    wins: Boolean,
    winColor: Color,
    modifier: Modifier = Modifier
) {
    // Counter animáció: számot felgörgetjük ha megváltozik
    val targetDouble = value.toDoubleOrNull()
    var displayValue by remember { mutableStateOf(value) }
    val animatedNumber = remember { Animatable(0f) }

    LaunchedEffect(value) {
        if (targetDouble != null) {
            animatedNumber.snapTo(0f)
            animatedNumber.animateTo(
                targetValue = targetDouble.toFloat(),
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
            displayValue = if (targetDouble % 1.0 == 0.0)
                targetDouble.toInt().toString()
            else targetDouble.toString()
        } else {
            displayValue = value
        }
    }

    val shownValue = if (targetDouble != null) {
        if (targetDouble % 1.0 == 0.0) animatedNumber.value.toInt().toString()
        else "%.1f".format(animatedNumber.value)
    } else {
        displayValue
    }

    // Háttér kiemelés
    val bgAlpha by animateFloatAsState(
        targetValue = if (wins) 0.15f else 0f,
        animationSpec = tween(600),
        label = "bgAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(winColor.copy(alpha = bgAlpha))
            .padding(vertical = 4.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shownValue,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = if (wins) FontWeight.Black else FontWeight.Normal
            ),
            color = if (wins) winColor else F1TextSec,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Clickable extension (nincs Foundation import konflikt miatt)
private fun Modifier.clickable(onClick: () -> Unit) =
    this.then(
        androidx.compose.foundation.clickable(onClick = onClick)
    )
