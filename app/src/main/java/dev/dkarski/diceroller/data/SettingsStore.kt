package dev.dkarski.diceroller.data

import android.content.Context

/** App preferences: vibration, keep screen on, theme and Coin presentation (language kept for future localization). */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** Vibration on app interactions (also off when touch vibration is off in the phone settings). */
    var vibration: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    /** BCP 47 tag of the chosen app language (see [AppLanguages]); null = follow the system language. */
    var languageTag: String?
        get() = prefs.getString(KEY_LANGUAGE, null)?.takeIf { AppLanguages.find(it) != null }
        set(value) = prefs.edit().apply { if (value == null) remove(KEY_LANGUAGE) else putString(KEY_LANGUAGE, value) }.apply()

    /** Display preference only. Coin always contributes 1 (Heads) or 2 (Tails). */
    var coinAsNumbers: Boolean
        get() = prefs.getBoolean(KEY_COIN_NUMBERS, false)
        set(value) = prefs.edit().putBoolean(KEY_COIN_NUMBERS, value).apply()

    /** Keeps the display awake while DiceRoller is in front, until 10 minutes pass without a roll. */
    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()

    /** Black background, white content; still pure black and white for E Ink. */
    var darkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    private companion object {
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_COIN_NUMBERS = "coin_as_numbers"
        const val KEY_LANGUAGE = "language"
        const val KEY_VIBRATION = "vibration"
    }
}
