package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val TraceLightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = OnGreen,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenDark,
    secondary = PeachPrimary,
    onSecondary = OnPeach,
    secondaryContainer = PeachContainer,
    onSecondaryContainer = OnPeachContainer,
    tertiary = TraceAmberTertiary,
    onTertiary = Color.White,
    tertiaryContainer = TraceAmberContainer,
    onTertiaryContainer = Color(0xFF5A3700),
    background = TraceBackground,
    onBackground = TraceTextPrimary,
    surface = TraceSurface,
    onSurface = TraceTextPrimary,
    surfaceVariant = TraceSurfaceVariant,
    onSurfaceVariant = TraceTextSecondary,
    outline = TraceOutline,
    outlineVariant = TraceOutlineVariant,
    error = TraceOnErrorContainer,
    onError = Color.White,
    errorContainer = TraceErrorContainer,
    onErrorContainer = TraceOnErrorContainer
)

private val TraceDarkColorScheme = darkColorScheme(
    primary = GreenLight,
    onPrimary = Color(0xFF042213),
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = GreenMint,
    secondary = PeachLight,
    onSecondary = Color(0xFF2B0B02),
    secondaryContainer = PeachContainerDark,
    onSecondaryContainer = PeachSoft,
    tertiary = PeachLight,
    onTertiary = Color(0xFF2B0B02),
    background = Color(0xFF101C18),
    onBackground = Color(0xFFF3F8F5),
    surface = Color(0xFF172621),
    onSurface = Color(0xFFF3F8F5),
    surfaceVariant = Color(0xFF20332C),
    onSurfaceVariant = Color(0xFFB3C7BE),
    outline = Color(0xFF395248),
    outlineVariant = Color(0xFF263B33)
)

@Composable
fun TraceTheme(
    darkTheme: Boolean = false, // Warm, motivating Peach & Green daylight canvas to avoid dull fatigue
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> TraceDarkColorScheme
        else -> TraceLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
