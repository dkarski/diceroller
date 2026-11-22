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
import com.mudita.mmd.eInkColorScheme

/**
 * Single source of the app's colours. The app has one look: MMD's E Ink scheme, black on white. Components read
 * colours from [MaterialTheme.colorScheme] (plus the helpers below);
 * never from hard-coded values. Only pure black and white are used (E Ink).
 */
val AppColors: ColorScheme = eInkColorScheme

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
fun AppTheme(content: @Composable () -> Unit) {
    // The Android 12 view root can retain the system direction after wrapping an app locale.
    val direction = if (LocalConfiguration.current.layoutDirection == View.LAYOUT_DIRECTION_RTL)
        LayoutDirection.Rtl else LayoutDirection.Ltr
    ThemeMMD(colorScheme = AppColors) {
        CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
    }
}
