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
import dev.dkarski.diceroller.ui.theme.AppColors

class MainActivity : ComponentActivity() {
    /** Applies the app language chosen in Settings (or keeps the system language). */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsStore = SettingsStore(this)
        applySystemBars()

        setContent {
            var vibration by remember { mutableStateOf(settingsStore.vibration) }
            AppTheme {
                App(
                    languageTag = settingsStore.languageTag,
                    vibration = vibration,
                    onVibrationChange = { on ->
                        vibration = on
                        settingsStore.vibration = on
                    },
                    onLanguageChange = { tag ->
                        if (tag != settingsStore.languageTag) {
                            settingsStore.languageTag = tag
                            // Rebuild resources while rememberSaveable restores navigation.
                            recreate()
                        }
                    },
                )
            }
        }
    }

    /** Window background and system bars in the app's colours: white with dark icons. */
    @Suppress("DEPRECATION") // statusBarColor/navigationBarColor are still the API on Android 12 (Kompakt).
    private fun applySystemBars() {
        val color = AppColors.background.toArgb()
        window.setBackgroundDrawable(ColorDrawable(color))
        window.statusBarColor = color
        window.navigationBarColor = color
        val lightBars = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        // window.insetsController is null-unsafe before the decor view exists (NPE on Android 12);
        // decorView creates it on first access.
        window.decorView.windowInsetsController?.setSystemBarsAppearance(lightBars, lightBars)
    }
}
