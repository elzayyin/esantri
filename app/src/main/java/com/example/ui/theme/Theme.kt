package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = PrimaryBlue,
    background = DarkBackground,
    surface = Color(0xFF1C2541),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryGreen,
    tertiary = Color(0xFF2563EB),
    background = SoftBlueBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

fun parseHexColor(hexString: String?, defaultColor: Color): Color {
    if (hexString.isNullOrEmpty()) return defaultColor
    return try {
        val cleanHex = hexString.trim().replace("#", "")
        if (cleanHex.length == 6) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else {
            defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primaryOverride: String? = null,
    secondaryOverride: String? = null,
    content: @Composable () -> Unit,
) {
    // If user updated colors, parse them, otherwise fall back to system defaults
    val activePrimary = parseHexColor(primaryOverride, if (darkTheme) PrimaryDark else PrimaryBlue)
    val activeSecondary = parseHexColor(secondaryOverride, if (darkTheme) SecondaryDark else SecondaryGreen)

    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = activePrimary,
            secondary = activeSecondary
        )
    } else {
        LightColorScheme.copy(
            primary = activePrimary,
            secondary = activeSecondary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
