# Translations

DiceRoller ships in the 63 languages offered by InkApps. English in `app/src/main/res/values/strings.xml` is the
source and the fallback. Each other language has its own `values-*/strings.xml`. All texts are in the APK and work
offline.

## How the language is chosen

- **System default**: the app follows the phone's language. MuditaOS K 2.0 has 9 system languages (English,
  Italiano, Polski, Čeština, Português, Español, Deutsch, Français, Nederlands). An unsupported phone language falls back
  to English.
- **Settings > Language**: any of the 63 languages, saved on the phone. The list shows System default, the 9 system
  languages, then the rest grouped by script. Each language is shown in its own name.

The code is in `data/AppLanguage.kt` (the list) and `data/AppLocale.kt` (applies the language to the Activity
context). It does not use AppCompat, and `LocaleManager` needs API 33.

## Resource folders

Most folders use the plain language code. These do not:

| Language | Folder | Why |
|---|---|---|
| Hebrew | `values-iw` | Android's legacy code for `he` |
| Indonesian | `values-in` | Android's legacy code for `id` |
| Filipino | `values-b+fil` | three-letter codes need the BCP 47 form |
| Chinese Simplified, Traditional | `values-b+zh+Hans`, `values-b+zh+Hant` | script, not region |
| Norwegian | `values-nb` | Bokmål |
| Serbian | `values-sr` | Cyrillic, Android's default script for Serbian |
| Portuguese, Spanish | `values-pt`, `values-es` | European usage; regional variants fall back to them |

`resourceConfigurations` in `app/build.gradle.kts` limits the APK to these languages, so library texts never appear
in a language the app does not offer.

## Terminology: dice

Die codes (D4–D20 and custom D%1$d) and the app name are not translated.
Translate the action, coin sides and Custom label in the language used for tabletop games.

Coin sides are display labels: Heads contributes 1 and Tails contributes 2 when combining, even when shown as words.
The Custom icon opens the editor; its separate label rolls. Preserve that distinction in accessibility labels.
The dice count uses `plurals`; include each language's required categories and keep `%1$d` in every form.

## Adding or changing a text

1. Edit English in `values/strings.xml`. Add a comment saying where the text appears and any length limit.
2. Run `scripts/i18n_check.py`. It lists every language that is missing the key.
3. Translate the key into all languages. Keep placeholders (`%1$s`, `%1$d`) and escaping (`\'`, `\n`).
4. Run `scripts/i18n_check.py` again (0 errors) and `./gradlew :app:lintDebug`, where `MissingTranslation` and
   `ExtraTranslation` are errors.
5. Check Home, coin results, Combine, Custom, Settings, Language, About and Tip jar on a 480×800 screen.
   Check long labels, larger system fonts, right-to-left layout and script rendering.
6. For a new script or language, check rendering on a Kompakt. See [device acceptance](ARCHITECTURE.md#device-acceptance).

## Quality

- All translations were generated with AI. A review by native speakers is recommended.
- Automated checks cover keys, placeholders, escaping and XML; lint checks plural coverage.
  These checks do not establish linguistic quality or on-device readability.
- Translation corrections are welcome as issues or pull requests. Include the language, screen and proposed text.
- The in-app privacy, terms and legal texts are translated for convenience. The English versions in `docs/` are the
  reference.
