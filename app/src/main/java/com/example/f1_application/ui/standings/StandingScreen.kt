package com.example.f1_application.ui.standings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import com.example.f1_application.data.model.HypraceConstructorStanding
import com.example.f1_application.data.model.HypraceDriverStanding
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.theme.*
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

    LaunchedEffect(username) { user = repository.getUser(username) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
    ) {
        // --- FEJLÉC ---
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(text = "BAJNOKSÁG", style = MaterialTheme.typography.headlineLarge, color = F1Red)
            Text(text = "CHAMPIONSHIP STANDINGS", style = MaterialTheme.typography.labelLarge, color = F1TextHint, letterSpacing = 3.sp)
        }

        // --- ÉV VÁLASZTÓ ---
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
        ) {
            OutlinedTextField(
                value = "$selectedYear SZEZON",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = F1Red,
                    unfocusedBorderColor = F1Border,
                    focusedTextColor = F1TextPrim,
                    unfocusedTextColor = F1TextPrim,
                    focusedContainerColor = F1Surface,
                    unfocusedContainerColor = F1Surface
                ),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(expanded, { expanded = false }, modifier = Modifier.background(F1Surface2)) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text("$year", color = F1TextPrim) },
                        onClick = { viewModel.setYear(year); expanded = false },
                        modifier = Modifier.background(F1Surface2)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- TÍPUS VÁLASZTÓ (DRIVER/CONSTRUCTOR) ---
        Row(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)).background(F1Surface).border(1.dp, F1Border, RoundedCornerShape(8.dp))
        ) {
            val isDriver = viewType == StandingViewType.DRIVER
            Box(
                modifier = Modifier.weight(1f).background(if (isDriver) F1Red else Color.Transparent)
                    .clickable { viewModel.setViewType(StandingViewType.DRIVER) }.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "EGYÉNI", style = MaterialTheme.typography.labelLarge, color = if (isDriver) F1TextPrim else F1TextHint)
            }
            Box(
                modifier = Modifier.weight(1f).background(if (!isDriver) F1Red else Color.Transparent)
                    .clickable { viewModel.setViewType(StandingViewType.CONSTRUCTOR) }.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "CSAPAT", style = MaterialTheme.typography.labelLarge, color = if (!isDriver) F1TextPrim else F1TextHint)
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- LISTA (JAVÍTOTT TÍPUSOKKAL) ---
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (viewType == StandingViewType.DRIVER) {
                // PILÓTÁK: Explicit típus HypraceDriverStanding
                itemsIndexed(driverStandings) { index, dStanding: HypraceDriverStanding ->
                    val isFavorite = user?.favoriteDriverId == dStanding.driverId
                    AnimatedStandingRow(
                        index = index,
                        pos = dStanding.position ?: 0,
                        name = dStanding.driverName ?: "Ismeretlen",
                        sub = "", // Pilótánál ide jöhetne a csapatnév, ha a modellben benne van
                        points = dStanding.points ?: 0.0,
                        isFavorite = isFavorite,
                        onFavoriteToggle = {
                            scope.launch {
                                repository.toggleFavoriteDriver(username, dStanding.driverId ?: "", dStanding.driverName ?: "")
                                user = repository.getUser(username)
                            }
                        }
                    )
                }
            } else {
                // CSAPATOK: Explicit típus HypraceConstructorStanding (ITT VOLT A HIBA)
                itemsIndexed(constructorStandings) { index, cStanding: HypraceConstructorStanding ->
                    val isFavorite = user?.favoriteTeamId == cStanding.teamId
                    AnimatedStandingRow(
                        index = index,
                        pos = cStanding.position ?: 0,
                        name = repository.toTitleCase(cStanding.teamName), // Most már látni fogja a teamName-t
                        sub = "",
                        points = cStanding.points ?: 0.0,
                        isFavorite = isFavorite,
                        onFavoriteToggle = {
                            scope.launch {
                                // JAVÍTVA: cStanding.teamName használata
                                repository.toggleFavoriteTeam(username, cStanding.teamId ?: "", cStanding.teamName ?: "")
                                user = repository.getUser(username)
                            }
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AnimatedStandingRow(
    index: Int,
    pos: Int,
    name: String,
    sub: String,
    points: Double,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 40L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(300))
    ) {
        val accentColor = when (pos) {
            1 -> F1Gold
            2 -> Color(0xFFBEC3C7)
            3 -> Color(0xFFCD7F32)
            else -> F1Border
        }

        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(F1Surface)
                .border(1.dp, accentColor.copy(alpha = if (pos <= 3) 0.5f else 0.15f), RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(48.dp).fillMaxHeight().background(accentColor.copy(alpha = 0.1f)).padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$pos", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black), color = if (pos <= 3) accentColor else F1TextHint)
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, tint = if (isFavorite) F1Gold else F1TextHint)
            }

            Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, color = F1TextPrim, fontWeight = FontWeight.Bold)
                if (sub.isNotEmpty()) Text(text = sub, style = MaterialTheme.typography.labelSmall, color = F1TextHint)
            }

            Text(
                text = if (points % 1.0 == 0.0) points.toInt().toString() else points.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black),
                color = if (pos == 1) F1Gold else F1Red,
                modifier = Modifier.padding(end = 14.dp)
            )
        }
    }
}