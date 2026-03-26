package com.example.f1_application.ui.compare

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.navigation.NavController
import com.example.f1_application.data.model.DriverStats
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.navigation.Screen
import com.example.f1_application.ui.theme.*

@Composable
fun CompareScreen(repository: F1Repository, username: String, navController: NavController) {
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
        modifier = Modifier.fillMaxSize().background(F1Dark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("COMPARE", style = MaterialTheme.typography.headlineLarge, color = F1Red)
                    Text("DRIVER HEAD-TO-HEAD", style = MaterialTheme.typography.labelLarge, color = F1TextHint, letterSpacing = 3.sp)
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
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DriverSearchField(modifier = Modifier.weight(1f), query = queryA, label = "DRIVER 1", accentColor = F1Red, isLoading = isLoadingA, error = errorA, onQueryChange = { viewModel.onQueryAChange(it) }, onSearch = { viewModel.searchDriverA() }, onClear = { viewModel.clearDriverA() })
                DriverSearchField(modifier = Modifier.weight(1f), query = queryB, label = "DRIVER 2", accentColor = F1Gold, isLoading = isLoadingB, error = errorB, onQueryChange = { viewModel.onQueryBChange(it) }, onSearch = { viewModel.searchDriverB() }, onClear = { viewModel.clearDriverB() })
            }
        }
        if (driverHistory.isNotEmpty()) {
            item {
                Text("QUICK LOAD", style = MaterialTheme.typography.labelLarge, color = F1TextHint, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(driverHistory.take(8)) { histItem ->
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(F1Surface)
                                .border(1.dp, F1Border, RoundedCornerShape(4.dp))
                                .clickable {
                                    if (driverA == null) viewModel.searchDriverA(histItem.query)
                                    else viewModel.searchDriverB(histItem.query)
                                }.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(histItem.query, style = MaterialTheme.typography.labelMedium, color = F1TextSec)
                        }
                    }
                }
            }
        }
        item {
            AnimatedVisibility(visible = driverA != null || driverB != null, enter = fadeIn(tween(500)) + expandVertically(tween(500)), exit = fadeOut() + shrinkVertically()) {
                CompareTable(driverA = driverA, driverB = driverB)
            }
        }
        item {
            AnimatedVisibility(visible = driverA == null && driverB == null, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(F1Surface)
                        .border(1.dp, F1Border, RoundedCornerShape(8.dp)).padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏎", fontSize = 36.sp)
                        Spacer(Modifier.height(10.dp))
                        Text("Search for two drivers\nto compare them!", textAlign = TextAlign.Center, color = F1TextHint, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}

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
            value = query, onValueChange = onQueryChange,
            placeholder = { Text(label, color = F1TextHint, style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.fillMaxWidth(), singleLine = true, isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = F1Border, focusedTextColor = F1TextPrim, unfocusedTextColor = F1TextPrim, cursorColor = accentColor, focusedContainerColor = F1Surface, unfocusedContainerColor = F1Surface, errorBorderColor = F1Red, errorContainerColor = F1Surface),
            trailingIcon = {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = accentColor)
                else if (query.isNotEmpty()) IconButton(onClick = onClear) { Icon(Icons.Default.Clear, null, tint = F1TextHint, modifier = Modifier.size(16.dp)) }
            },
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = F1TextPrim)
        )
        error?.let { Text(it, color = F1Red, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp)) }
        Button(
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = F1TextPrim),
            shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("SEARCH", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun CompareTable(driverA: DriverStats?, driverB: DriverStats?) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(F1Surface).border(1.dp, F1Border, RoundedCornerShape(8.dp))) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1.3f))
                Text(driverA?.fullName?.split(" ")?.last()?.uppercase() ?: "–", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge, color = F1Red, fontWeight = FontWeight.Black)
                Text(driverB?.fullName?.split(" ")?.last()?.uppercase() ?: "–", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge, color = F1Gold, fontWeight = FontWeight.Black)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = F1Border)
            CompareRow("TEAM", driverA?.currentTeam, driverB?.currentTeam, higherIsBetter = false)
            CompareRow("WINS", driverA?.wins?.toDouble(), driverB?.wins?.toDouble())
            CompareRow("PODIUMS", driverA?.podiums?.toDouble(), driverB?.podiums?.toDouble())
            CompareRow("POLES", driverA?.totalPoles?.toDouble(), driverB?.totalPoles?.toDouble())
            CompareRow("POINTS", driverA?.totalPoints, driverB?.totalPoints)
            CompareRow("BEST POS.", driverA?.bestPosition?.toDouble(), driverB?.bestPosition?.toDouble(), higherIsBetter = false)
            CompareRow("ACTIVE YEARS", driverA?.activeYears, driverB?.activeYears, higherIsBetter = false)
        }
    }
}

@Composable
fun CompareRow(label: String, valueA: Any?, valueB: Any?, higherIsBetter: Boolean = true) {
    val aDouble = valueA as? Double
    val bDouble = valueB as? Double
    val aWins = aDouble != null && bDouble != null && if (higherIsBetter) aDouble > bDouble else aDouble < bDouble
    val bWins = aDouble != null && bDouble != null && if (higherIsBetter) bDouble > aDouble else bDouble < aDouble

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelSmall, color = F1TextHint)
        AnimatedCompareCell(value = valueA?.toString() ?: "–", wins = aWins, winColor = F1Red, modifier = Modifier.weight(1f))
        AnimatedCompareCell(value = valueB?.toString() ?: "–", wins = bWins, winColor = F1Gold, modifier = Modifier.weight(1f))
    }
}

@Composable
fun AnimatedCompareCell(value: String, wins: Boolean, winColor: Color, modifier: Modifier = Modifier) {
    val targetDouble = value.toDoubleOrNull()
    var displayValue by remember { mutableStateOf(value) }
    val animatedNumber = remember { Animatable(0f) }

    LaunchedEffect(value) {
        if (targetDouble != null) {
            animatedNumber.snapTo(0f)
            animatedNumber.animateTo(targetValue = targetDouble.toFloat(), animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing))
            displayValue = if (targetDouble % 1.0 == 0.0) targetDouble.toInt().toString() else targetDouble.toString()
        } else { displayValue = value }
    }

    val shownValue = if (targetDouble != null) {
        if (targetDouble % 1.0 == 0.0) animatedNumber.value.toInt().toString() else "%.1f".format(animatedNumber.value)
    } else displayValue

    val bgAlpha by animateFloatAsState(targetValue = if (wins) 0.15f else 0f, animationSpec = tween(600), label = "bgAlpha")

    Box(modifier = modifier.clip(RoundedCornerShape(4.dp)).background(winColor.copy(alpha = bgAlpha)).padding(vertical = 4.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
        Text(shownValue, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = if (wins) FontWeight.Black else FontWeight.Normal), color = if (wins) winColor else F1TextSec, modifier = Modifier.fillMaxWidth())
    }
}