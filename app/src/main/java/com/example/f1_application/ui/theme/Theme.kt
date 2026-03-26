package com.example.f1_application.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val F1DarkColorScheme = darkColorScheme(
    primary = F1Red,
    secondary = Color.Gray,
    background = F1Dark,
    surface = F1Dark,
    onPrimary = White,
    onBackground = White,
    onSurface = White
)


private val F1LightColorScheme = lightColorScheme(
    primary = F1Red,
    secondary = Color.DarkGray,
    background = White,
    surface = White,
    onPrimary = White,
    onBackground = F1Black,
    onSurface = F1Black
)

@Composable
fun F1applicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) F1DarkColorScheme else F1LightColorScheme


    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}