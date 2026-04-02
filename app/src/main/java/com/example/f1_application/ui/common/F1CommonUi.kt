package com.example.f1_application.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.f1_application.ui.theme.*

// ─── Állandó színek (közvetlenül a theme-ből) ───────────────────────
val CardBg    = F1Surface
val CardBg2   = F1Surface2
val Border    = F1Border
val RedAccent = F1Red
val Gold      = F1Gold

// ─── F1 stílusú szekció fejléc (pl. "KEDVENC PILÓTÁD") ─────────────
@Composable
fun F1SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .background(F1Red, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = F1TextSec,
            letterSpacing = 2.sp
        )
    }
}

// ─── F1 kártya alap (sötét háttér, piros bal border) ───────────────
@Composable
fun F1Card(
    modifier: Modifier = Modifier,
    accentColor: Color = F1Red,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(F1Surface)
            .border(1.dp, F1Border, RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .matchParentSize()
                .background(accentColor)
        )
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
            content = content
        )
    }
}

// ─── Stat cella (GYŐZELEM / 77) ──────────────────────────────────────
@Composable
fun F1StatCell(
    label: String,
    value: String,
    valueColor: Color = F1Red,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = F1TextHint,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            ),
            color = valueColor,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Pill badge (pl. "Befejezve") ───────────────────────────────────
@Composable
fun F1Badge(
    text: String,
    color: Color = F1Red,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

// ─── Pulzáló animáció (visszaszámlálóhoz) ───────────────────────────
@Composable
fun pulsingAlpha(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    return alpha
}

// ─── Vízszintes elválasztó F1 stílusban ─────────────────────────────
@Composable
fun F1Divider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = F1Border
    )
}
