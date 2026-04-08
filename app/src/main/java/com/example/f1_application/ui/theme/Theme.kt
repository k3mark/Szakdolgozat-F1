package com.example.f1_application.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val F1ColorScheme = darkColorScheme(
    primary            = F1Red,
    onPrimary          = F1TextPrim,
    secondary          = F1Gold,
    onSecondary        = F1Dark,
    tertiary           = F1Orange,
    onTertiary         = F1Dark,
    background         = F1Dark,
    onBackground       = F1TextPrim,
    surface            = F1Surface,
    onSurface          = F1TextPrim,
    surfaceVariant     = F1Surface2,
    onSurfaceVariant   = F1TextSec,
    outline            = F1Border,
    error              = F1Red,
    onError            = F1TextPrim,
)

@Composable
fun F1applicationTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = F1ColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = F1Dark.toArgb()
            window.navigationBarColor = F1Dark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
