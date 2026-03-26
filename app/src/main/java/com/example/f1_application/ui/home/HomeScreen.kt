package com.example.f1_application.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.common.*
import com.example.f1_application.ui.navigation.Screen
import com.example.f1_application.ui.search.CircuitResultCard
import com.example.f1_application.ui.search.DriverResultCard
import com.example.f1_application.ui.theme.*

@Composable
fun HomeScreen(username: String, repository: F1Repository, navController: NavController) {
    val viewModel: HomeViewModel = viewModel()
    val nextRace by viewModel.nextRace.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val favDriver by viewModel.favoriteDriver.collectAsState()
    val favTeamName by viewModel.favoriteTeamName.collectAsState()
    val favHistory by viewModel.favoriteTeamHistory.collectAsState()
    val favCircuit by viewModel.favoriteCircuit.collectAsState()
    val favTrackTime by viewModel.favoriteTrackCountdown.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadFavoritesData(username)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── HEADER with profile avatar ────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "F1 DASHBOARD",
                        style = MaterialTheme.typography.headlineLarge,
                        color = F1Red
                    )
                    Text(
                        text = "RACE CONTROL CENTER",
                        style = MaterialTheme.typography.labelLarge,
                        color = F1TextHint,
                        letterSpacing = 4.sp
                    )
                }
                // Profile avatar — navigates to Profile screen
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(F1Surface)
                        .border(2.dp, F1Red, CircleShape)
                        .clickable { navController.navigate(Screen.Profile.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.take(2).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = F1Red,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // ── NEXT GRAND PRIX ──────────────────────────────────────
        item {
            nextRace?.let { race ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(F1Surface)
                        .border(1.dp, F1Red.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        F1Badge("NEXT GRAND PRIX", color = F1Red)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = race.raceName ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            color = F1TextPrim,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        val alpha = pulsingAlpha()
                        Text(
                            text = countdown,
                            style = MaterialTheme.typography.displaySmall,
                            color = F1Red.copy(alpha = alpha),
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // ── FAVORITE DRIVER ──────────────────────────────────────
        item {
            F1SectionHeader("FAVORITE DRIVER")
            if (favDriver != null) {
                DriverResultCard(favDriver!!)
            } else {
                F1EmptyState("No favorite driver set.\nStar one in Standings!")
            }
        }

        // ── FAVORITE TEAM ────────────────────────────────────────
        item {
            F1SectionHeader("FAVORITE TEAM")
            if (favHistory.isNotEmpty()) {
                val current = favHistory.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(F1Surface)
                        .border(1.dp, F1Border, RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(F1Gold)
                    )
                    Column(Modifier.padding(start = 16.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)) {
                        Text(
                            text = favTeamName ?: "Unknown team",
                            style = MaterialTheme.typography.headlineSmall,
                            color = F1TextPrim,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth()) {
                            F1DataTile(
                                label = "POINTS (2026)",
                                value = "${current.points.toInt()} PTS",
                                valueColor = F1Gold,
                                modifier = Modifier.weight(1f)
                            )
                            F1DataTile(
                                label = "POSITION",
                                value = "${current.position}. PLACE",
                                valueColor = F1TextPrim,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                F1EmptyState("No favorite team set.\nStar one in Standings!")
            }
        }

        // ── FAVORITE TRACK ───────────────────────────────────────
        item {
            F1SectionHeader("FAVORITE TRACK & RACE")
            if (favCircuit != null) {
                CircuitResultCard(favCircuit!!)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(F1Surface)
                        .border(1.dp, F1Orange.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "TIME LEFT UNTIL FAVORITE RACE",
                            style = MaterialTheme.typography.labelLarge,
                            color = F1TextHint,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        val alpha = pulsingAlpha()
                        Text(
                            text = favTrackTime,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = F1Orange.copy(alpha = alpha),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                F1EmptyState("No favorite track set.\nStar one in Calendar!")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun F1EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(F1Surface)
            .border(1.dp, F1Border, RoundedCornerShape(8.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = F1TextHint,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun F1DataTile(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = F1Red,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = F1TextHint
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            ),
            color = valueColor
        )
    }
}