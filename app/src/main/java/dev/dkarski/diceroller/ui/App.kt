package dev.dkarski.diceroller.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.dkarski.diceroller.AppConfig
import dev.dkarski.diceroller.R
import dev.dkarski.diceroller.data.AppLanguages
import dev.dkarski.diceroller.ui.components.SecondaryLayout

enum class Screen { Home, Settings, Language, About, TipJar }

private fun Screen.parent(): Screen? = when (this) {
    Screen.Home -> null
    Screen.Settings -> Screen.Home
    Screen.Language, Screen.About, Screen.TipJar -> Screen.Settings
}

/** Navigation and shared preferences. Add domain state/screens here when starting an app. */
@Composable
fun App(
    languageTag: String?,
    onLanguageChange: (String?) -> Unit,
    vibration: Boolean,
    onVibrationChange: (Boolean) -> Unit,
    homeContent: @Composable () -> Unit = {},
    appSettings: (@Composable () -> Unit)? = null,
    settingsBottomAction: (@Composable () -> Unit)? = null,
) {
    val context = LocalContext.current
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    val screenStates = rememberSaveableStateHolder()
    val back: () -> Unit = { screen.parent()?.let { screen = it } }
    BackHandler(enabled = screen.parent() != null, onBack = back)
    val about = remember(context) {
        AboutInfo(
            context.getString(R.string.app_name),
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty(),
            AppConfig.DEVELOPER_NAME,
        )
    }
    var emailAvailable by remember { mutableStateOf(hasEmailApp(context)) }
    var contactCopied by rememberSaveable { mutableStateOf(false) }
    val supportEnabled = isSupportUrl(AppConfig.SUPPORT_URL)
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        screenStates.SaveableStateProvider(screen.name) {
            when (screen) {
                Screen.Home -> HomeScreen(onSettings = { screen = Screen.Settings }, content = homeContent)
                Screen.Settings -> SecondaryLayout(stringResource(R.string.settings_title), back) { padding ->
                    SettingsScreen(
                        vibration = vibration,
                        onVibrationChange = onVibrationChange,
                        languageName = AppLanguages.find(languageTag)?.nativeName ?: stringResource(R.string.language_system_default),
                        onLanguage = { screen = Screen.Language },
                        onAbout = { contactCopied = false; screen = Screen.About },
                        onTipJar = if (supportEnabled) { { screen = Screen.TipJar } } else null,
                        contentPadding = padding,
                        appSettings = appSettings,
                        bottomAction = settingsBottomAction,
                    )
                }
                Screen.Language -> LanguageScreen(languageTag, onSelect = { tag ->
                    screen = Screen.Settings
                    onLanguageChange(tag)
                }, onBack = back)
                Screen.About -> SecondaryLayout(stringResource(R.string.about_title), back) { padding ->
                    AboutScreen(
                        about = about,
                        contact = if (AppConfig.DEVELOPER_EMAIL.isBlank()) null else
                            ContactState(AppConfig.DEVELOPER_EMAIL, emailAvailable, contactCopied),
                        onContact = { if (!openContactEmail(context, AppConfig.CONTACT_SUBJECT)) emailAvailable = false },
                        onCopyEmail = { copyDeveloperEmail(context); contactCopied = true },
                        contentPadding = padding,
                    )
                }
                Screen.TipJar -> SecondaryLayout(stringResource(R.string.about_tip_jar), back) { padding ->
                    TipJarScreen(AppConfig.SUPPORT_URL, padding)
                }
            }
        }
    }
}
