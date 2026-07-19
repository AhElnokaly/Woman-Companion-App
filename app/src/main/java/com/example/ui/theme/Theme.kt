package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Primitives.VibrantRoseDark,
    secondary = Primitives.OceanMintDark,
    tertiary = Primitives.LightPinkGlow,
    background = Primitives.CosmicNavy,
    surface = Primitives.MidnightSlate,
    onPrimary = Primitives.CosmicNavy,
    onSecondary = Primitives.CosmicNavy,
    onBackground = Primitives.LightPinkGlow,
    onSurface = Primitives.LightPinkGlow
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Primitives.PassionRoseLight,
    secondary = Primitives.ForestTealLight,
    tertiary = Primitives.SoftRoseLight,
    background = Primitives.BlossomCream,
    surface = Primitives.PureWhite,
    onPrimary = Primitives.PureWhite,
    onSecondary = Primitives.PureWhite,
    onBackground = Primitives.SlateBlueLight,
    onSurface = Primitives.SlateBlueLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to strictly enforce our custom professional palette
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
