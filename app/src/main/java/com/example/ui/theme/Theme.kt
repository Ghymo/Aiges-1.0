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

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldAccent,
    onPrimary = DarkNavy,
    secondary = GoldAccent,
    onSecondary = PureWhite,
    tertiary = SafeGreen,
    background = DarkBackground,
    onBackground = TextColorDark,
    surface = DarkSurface,
    onSurface = TextColorDark,
    error = DangerRed,
    onError = PureWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DarkNavy,
    onPrimary = PureWhite,
    secondary = GoldAccent,
    onSecondary = DarkNavy,
    tertiary = SafeGreen,
    background = LightBackground,
    onBackground = TextColorLight,
    surface = PureWhite,
    onSurface = TextColorLight,
    error = DangerRed,
    onError = PureWhite
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic colors so our customized Navy and Gold corporate brand is visible on all platforms
  dynamicColor: Boolean = false,
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
