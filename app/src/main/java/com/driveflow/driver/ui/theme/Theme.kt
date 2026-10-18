package com.driveflow.driver.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandGold,
    onPrimary = BrandNavyBackground,
    primaryContainer = BrandGoldDark,
    onPrimaryContainer = TextWhite,
    secondary = BrandMint,
    onSecondary = BrandNavyBackground,
    secondaryContainer = BrandMintDark,
    onSecondaryContainer = TextWhite,
    background = BrandNavyBackground,
    onBackground = TextWhite,
    surface = BrandNavySurface,
    onSurface = TextWhite,
    surfaceVariant = BrandNavySurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = BrandNavyBorder,
    error = VerdictRed,
    onError = TextWhite
)

@Composable
fun DriveFlowTheme(
    isRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BrandNavyBackground.toArgb()
                window.navigationBarColor = BrandNavyBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
