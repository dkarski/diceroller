package dev.dkarski.diceroller.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import dev.dkarski.diceroller.data.RollSession
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import dev.dkarski.diceroller.ui.components.DottedDivider
import dev.dkarski.diceroller.ui.components.SecondaryLayout

/** Keep screen on releases the display after this long without a roll or other action. */
private const val KEEP_AWAKE_MILLIS = 10 * 60 * 1000L

enum class Screen { Home, CustomDie, Settings, About, TipJar }

private fun Screen.parent(): Screen? = when (this) {
    Screen.Home -> null
    Screen.CustomDie, Screen.Settings -> Screen.Home
    Screen.About, Screen.TipJar -> Screen.Settings
}

/** Navigation and shared preferences. Add domain state/screens here when starting an app. */
@Composable
fun App(
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    vibration: Boolean,
    onVibrationChange: (Boolean) -> Unit,
    coinAsNumbers: Boolean,
    onCoinAsNumbersChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var session by rememberSaveable(stateSaver = RollSessionSaver) { mutableStateOf(RollSession()) }
    // Every accepted action counts as activity for Keep screen on.
    var activity by remember { mutableIntStateOf(0) }
    fun feedback() {
        activity++
        if (vibration) view.vibrate(Haptic.Tap)
    }
    // Keep screen on: only while DiceRoller is in front (a window flag, no permission), and released after
    // KEEP_AWAKE_MILLIS without activity so a forgotten open app cannot drain the battery.
    LaunchedEffect(keepScreenOn, activity) {
        view.keepScreenOn = keepScreenOn
        if (keepScreenOn) {
            delay(KEEP_AWAKE_MILLIS)
            view.keepScreenOn = false
        }
    }
    DisposableEffect(view) { onDispose { view.keepScreenOn = false } }
    // This effect belongs to the app, not Home: time spent in Settings does not restart the timeout.
    LaunchedEffect(session.latest) {
        val expected = session.latest ?: return@LaunchedEffect
        val remaining = expected.expiresAtMillis - SystemClock.elapsedRealtime()
        if (remaining > 0) delay(remaining)
        session = session.expire(expected, SystemClock.elapsedRealtime())
    }
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    val screenStates = rememberSaveableStateHolder()
    val back: () -> Unit = { screen.parent()?.let { screen = it } }
    BackHandler(enabled = screen.parent() != null, onBack = back)
    // On Home, system Back ends Combine (same as Done) instead of leaving the app.
    BackHandler(enabled = screen == Screen.Home && session.combining) {
        session = session.setCombining(false); feedback()
    }
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
                Screen.Home -> DiceScreen(
                    session = session,
                    coinAsNumbers = coinAsNumbers,
                    onRoll = { kind -> session = session.roll(kind, SystemClock.elapsedRealtime()); feedback() },
                    onCombiningChange = { enabled -> session = session.setCombining(enabled); feedback() },
                    onEditCustom = {
                        // Forget any earlier, unsaved edit: the editor always opens on the current range, selected.
                        screenStates.removeState(Screen.CustomDie.name)
                        session = session.dismissLatest()
                        screen = Screen.CustomDie
                        feedback()
                    },
                    onReroll = { if (session.canReroll) { session = session.reroll(); feedback() } },
                    // The single result is momentary: it does not wait on the dice screen while Settings is open.
                    onSettings = { session = session.dismissLatest(); screen = Screen.Settings },
                )
                Screen.CustomDie -> CustomDieScreen(
                    sides = session.customSides,
                    onSave = { sides -> session = session.editCustom(sides); screen = Screen.Home; feedback() },
                    onBack = back,
                )
                Screen.Settings -> SecondaryLayout(stringResource(R.string.settings_title), back) { padding ->
                    SettingsScreen(
                        vibration = vibration,
                        onVibrationChange = onVibrationChange,
                        // 1.0 is English-only: the row shows the language without opening a picker.
                        languageName = stringResource(R.string.language_english),
                        onLanguage = null,
                        onAbout = { contactCopied = false; screen = Screen.About },
                        onTipJar = if (supportEnabled) { { screen = Screen.TipJar } } else null,
                        contentPadding = padding,
                        appSettings = {
                            SettingsSwitchRow(
                                label = stringResource(R.string.keep_screen_on),
                                checked = keepScreenOn,
                                onCheckedChange = { onKeepScreenOnChange(it); feedback() },
                            )
                            DottedDivider()
                            SettingsSwitchRow(
                                label = stringResource(R.string.dark_theme),
                                checked = darkTheme,
                                onCheckedChange = { onDarkThemeChange(it); feedback() },
                            )
                            DottedDivider()
                            SettingsSwitchRow(
                                label = stringResource(R.string.coin_as_numbers),
                                checked = coinAsNumbers,
                                onCheckedChange = { onCoinAsNumbersChange(it); feedback() },
                            )
                        },
                    )
                }
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
