package dev.dkarski.diceroller.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import android.view.View
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mudita.mmd.ThemeMMD
import com.mudita.mmd.black
import com.mudita.mmd.eInkColorScheme
import com.mudita.mmd.white

/**
 * Single source of the app's colours. Components read colours from [MaterialTheme.colorScheme] (plus the helpers
 * below), never from hard-coded values. Only pure black and white are used (E Ink).
 */
val AppColors: ColorScheme = eInkColorScheme

/** MMD has no dark scheme: its E Ink scheme with black and white swapped (still pure 1-bit). From lifepoints 1.1. */
val DarkColors: ColorScheme = eInkColorScheme.run {
    copy(
        primary = primary.inverted(), onPrimary = onPrimary.inverted(),
        primaryContainer = primaryContainer.inverted(), onPrimaryContainer = onPrimaryContainer.inverted(),
        inversePrimary = inversePrimary.inverted(),
        secondary = secondary.inverted(), onSecondary = onSecondary.inverted(),
        secondaryContainer = secondaryContainer.inverted(), onSecondaryContainer = onSecondaryContainer.inverted(),
        tertiary = tertiary.inverted(), onTertiary = onTertiary.inverted(),
        tertiaryContainer = tertiaryContainer.inverted(), onTertiaryContainer = onTertiaryContainer.inverted(),
        background = background.inverted(), onBackground = onBackground.inverted(),
        surface = surface.inverted(), onSurface = onSurface.inverted(),
        surfaceVariant = surfaceVariant.inverted(), onSurfaceVariant = onSurfaceVariant.inverted(),
        surfaceTint = surfaceTint.inverted(),
        inverseSurface = inverseSurface.inverted(), inverseOnSurface = inverseOnSurface.inverted(),
        error = error.inverted(), onError = onError.inverted(),
        errorContainer = errorContainer.inverted(), onErrorContainer = onErrorContainer.inverted(),
        outline = outline.inverted(), outlineVariant = outlineVariant.inverted(),
        scrim = scrim.inverted(),
        surfaceContainerLow = surfaceContainerLow.inverted(),
    )
}

private fun Color.inverted(): Color = when (this) {
    black -> white
    white -> black
    else -> this
}

fun appColors(dark: Boolean): ColorScheme = if (dark) DarkColors else AppColors

/** Alpha for "not available yet" content. The only place where grey appears (E Ink rule). */
private const val MUTED_ALPHA = 0.6f

/** Content colour for disabled / coming-soon elements. */
val ColorScheme.muted: Color get() = onSurface.copy(alpha = MUTED_ALPHA)

/** Border width shared by tiles, outlined buttons and counter controls. */
val BorderWidth = 2.dp

/** Outline in the scheme's colour (MMD's OutlinedButton border is hard-coded). */
@Composable
@ReadOnlyComposable
fun outlineBorder(muted: Boolean = false): BorderStroke =
    BorderStroke(BorderWidth, if (muted) MaterialTheme.colorScheme.muted else MaterialTheme.colorScheme.outline)

@Composable
fun AppTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    // The Android 12 view root can retain the system direction after wrapping an app locale.
    val direction = if (LocalConfiguration.current.layoutDirection == View.LAYOUT_DIRECTION_RTL)
        LayoutDirection.Rtl else LayoutDirection.Ltr
    ThemeMMD(colorScheme = appColors(dark)) {
        CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
    }
}
