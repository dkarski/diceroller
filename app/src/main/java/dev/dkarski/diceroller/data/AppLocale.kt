package dev.dkarski.diceroller.data

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

/**
 * Per-app language on Android 12 without extra libraries.
 *
 * Android's own per-app language API (LocaleManager) starts at API 33 and the AppCompat backport would add a large
 * dependency, so the Activity wraps its base context with the chosen locale instead. Resources then resolve to the
 * matching `values-*` folder and the layout direction follows the locale (RTL for Hebrew, Persian, Urdu).
 * Only the active language's strings are ever read; nothing else is loaded.
 */
object AppLocale {
    /**
     * Called from Activity.attachBaseContext. DiceRoller 1.0 is English-only (docs/CONTINUATION.md): the inherited
     * starter translations would otherwise mix with the English-only dice labels. The language picker and the
     * translation folders stay in the code base for a future full localization.
     */
    fun wrap(base: Context): Context {
        val locale = Locale.ENGLISH
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration).apply { setLocale(locale) } // also sets layout direction
        return base.createConfigurationContext(config)
    }

    /** The language set in the phone's settings, independent of the app's choice. */
    fun systemLocale(): Locale = Resources.getSystem().configuration.locales[0]
}
