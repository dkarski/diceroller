package dev.dkarski.diceroller

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import dev.dkarski.diceroller.data.AppLocale
import dev.dkarski.diceroller.data.SettingsStore
import dev.dkarski.diceroller.ui.App
import dev.dkarski.diceroller.ui.theme.AppTheme
import dev.dkarski.diceroller.ui.theme.appColors

class MainActivity : ComponentActivity() {
    /** Applies the app language chosen in Settings (or keeps the system language). */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsStore = SettingsStore(this)
        applySystemBars(settingsStore.darkTheme)

        setContent {
            var coinAsNumbers by remember { mutableStateOf(settingsStore.coinAsNumbers) }
            var vibration by remember { mutableStateOf(settingsStore.vibration) }
            var keepScreenOn by remember { mutableStateOf(settingsStore.keepScreenOn) }
            var darkTheme by remember { mutableStateOf(settingsStore.darkTheme) }
            AppTheme(dark = darkTheme) {
                App(
                    languageTag = settingsStore.languageTag,
                    onLanguageChange = { tag ->
                        if (tag != settingsStore.languageTag) {
                            settingsStore.languageTag = tag
                            // Rebuild resources while rememberSaveable restores navigation.
                            recreate()
                        }
                    },
                    keepScreenOn = keepScreenOn,
                    onKeepScreenOnChange = { on ->
                        keepScreenOn = on
                        settingsStore.keepScreenOn = on
                    },
                    darkTheme = darkTheme,
                    onDarkThemeChange = { on ->
                        darkTheme = on
                        settingsStore.darkTheme = on
                        applySystemBars(on)
                    },
                    coinAsNumbers = coinAsNumbers,
                    onCoinAsNumbersChange = { numeric ->
                        coinAsNumbers = numeric
                        settingsStore.coinAsNumbers = numeric
                    },
                    vibration = vibration,
                    onVibrationChange = { on ->
                        vibration = on
                        settingsStore.vibration = on
                    },
                )
            }
        }
    }

    /** Window background and system bars follow the theme (no white flash in Dark theme). */
    @Suppress("DEPRECATION") // statusBarColor/navigationBarColor are still the API on Android 12 (Kompakt).
    private fun applySystemBars(dark: Boolean) {
        val color = appColors(dark).background.toArgb()
        window.setBackgroundDrawable(ColorDrawable(color))
        window.statusBarColor = color
        window.navigationBarColor = color
        val lightBars = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        // window.insetsController is null-unsafe before the decor view exists (NPE on Android 12);
        // decorView creates it on first access.
        window.decorView.windowInsetsController?.setSystemBarsAppearance(if (dark) 0 else lightBars, lightBars)
    }
}
