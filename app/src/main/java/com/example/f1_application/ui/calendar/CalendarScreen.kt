package com.example.f1_application.ui.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.theme.*
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

    LaunchedEffect(username) { user = repository.getUser(username) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
    ) {
        // ── FEJLÉC ───────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "NAPTÁR",
                style = MaterialTheme.typography.headlineLarge,
                color = F1Red
            )
            Text(
                text = "RACE CALENDAR",
                style = MaterialTheme.typography.labelLarge,
                color = F1TextHint,
                letterSpacing = 3.sp
            )
        }

        // ── ÉV VÁLASZTÓ ──────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
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
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 2.sp,
                    color = F1TextSec
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(F1Surface2)
            ) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text("$year", color = F1TextPrim, style = MaterialTheme.typography.bodyMedium) },
                        onClick = { viewModel.updateYear(year); expanded = false },
                        modifier = Modifier.background(F1Surface2)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── FUTAM LISTA ──────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(races) { index, race ->
                val isFav = race.circuitId != null && user?.favoriteTrackId == race.circuitId
                val isFinished = race.status == "Finished"

                AnimatedCalendarRow(
                    index = index,
                    race = race,
                    isFav = isFav,
                    isFinished = isFinished,
                    onFavClick = {
                        if (race.circuitId != null) {
                            scope.launch {
                                repository.toggleFavoriteTrack(
                                    username = username,
                                    trackId = race.circuitId!!,
                                    gpName = race.raceName ?: "Ismeretlen Nagydíj",
                                    circuitName = race.officialName ?: "Ismeretlen pálya"
                                )
                                user = repository.getUser(username)
                            }
                        }
                    }
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AnimatedCalendarRow(
    index: Int,
    race: com.example.f1_application.data.model.HypraceRace,
    isFav: Boolean,
    isFinished: Boolean,
    onFavClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 35L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(300)
        )
    ) {
        val roundNum = index + 1
        val accentColor = if (isFav) F1Gold else if (isFinished) F1TextHint else F1Red

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(F1Surface)
                .border(
                    1.dp,
                    accentColor.copy(alpha = if (isFav) 0.5f else 0.15f),
                    RoundedCornerShape(8.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kör szám
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .background(
                        if (isFinished) F1Surface2
                        else F1Red.copy(alpha = 0.1f)
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "R",
                        style = MaterialTheme.typography.labelSmall,
                        color = F1TextHint,
                        fontSize = 8.sp
                    )
                    Text(
                        text = "$roundNum",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        ),
                        color = if (isFinished) F1TextHint else F1Red
                    )
                }
            }

            // Kedvenc csillag
            IconButton(
                onClick = onFavClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (isFav) F1Gold else F1TextHint,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Futam adatok
            Column(
                Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = race.raceName ?: "Ismeretlen",
                    style = MaterialTheme.typography.titleMedium,
                    color = F1TextPrim,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = race.officialName ?: "Ismeretlen pálya",
                    style = MaterialTheme.typography.labelSmall,
                    color = F1Red
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = formatF1Date(race.startDate ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = F1TextHint,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Státusz badge
            Box(
                modifier = Modifier.padding(end = 12.dp)
            ) {
                if (isFinished) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(F1TextHint.copy(alpha = 0.1f))
                            .border(1.dp, F1TextHint.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "KÉSZ",
                            style = MaterialTheme.typography.labelSmall,
                            color = F1TextHint
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(F1Red.copy(alpha = 0.1f))
                            .border(1.dp, F1Red.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "VÁRHATÓ",
                            style = MaterialTheme.typography.labelSmall,
                            color = F1Red
                        )
                    }
                }
            }
        }
    }
}

fun formatF1Date(rawDate: String): String =
    if (rawDate.length >= 10) rawDate.substring(0, 10).replace("-", ". ") else rawDate
