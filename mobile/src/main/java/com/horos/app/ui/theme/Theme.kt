package com.horos.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HorosColorScheme = lightColorScheme(
    primary = HorosBlue,
    onPrimary = Color.White,
    secondary = HorosNavy,
    background = Background,
    onBackground = TextPrimary,
    surface = Background,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = PeriodCardBorder,
    error = StatusOver
)

@Composable
fun HorosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HorosColorScheme,
        typography = Typography,
        content = content
    )
}
