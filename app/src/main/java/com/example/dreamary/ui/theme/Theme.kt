package com.example.dreamary.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = Color(0xFFEEDFF7),
    secondary = Secondary,
    onSecondary = Color(0xFF1A1508),
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = Color(0xFFFCF2E2),
    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF497A6D),
    onTertiaryContainer = Color(0xFFE0F2ED),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = Primary.copy(alpha = 0.05f),
    inverseSurface = Color(0xFFE9E4EF),
    inverseOnSurface = Color(0xFF332C42),
    outline = DarkOutline,
    outlineVariant = Color(0xFF433A58),
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFF8C3333),
    onErrorContainer = Color(0xFFFFDADA),
    scrim = Color.Black.copy(alpha = 0.4f)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1E9F6),
    onPrimaryContainer = Color(0xFF593D6B),
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFAEDD1),
    onSecondaryContainer = Color(0xFF7D5A26),
    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDEEDE8),
    onTertiaryContainer = Color(0xFF2F4840),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = Primary.copy(alpha = 0.05f),
    inverseSurface = Color(0xFF3D3A43),
    inverseOnSurface = Color(0xFFFCF9F5),
    outline = LightOutline,
    outlineVariant = Color(0xFFE5DED3),
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFADEDE),
    onErrorContainer = Color(0xFF8C3333),
    scrim = Color.Black.copy(alpha = 0.25f)
)

@Composable
fun DreamaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Mise à jour de la couleur de la barre système
//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = colorScheme.primary.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
//        }
//    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}