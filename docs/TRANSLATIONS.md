# Translations

DiceRoller includes English and 62 translations. All texts are bundled in the APK and work offline.
English in `app/src/main/res/values/strings.xml` is the source and fallback.

## Language selection

System default follows the phone's language. Settings > Language selects a language independently and saves
the choice. Languages appear under their own names, with search. See `data/AppLanguage.kt` and `data/AppLocale.kt`.

## Editing translations

1. Edit the English source and every translation affected by the change.
2. Preserve format arguments such as `%1$s` and `%1$d`, and Android XML escaping.
3. Translate `plurals` for each language's grammatical categories; the dice count must keep its numeric argument.
4. Run `python3 scripts/i18n_check.py --quiet` and `./gradlew :app:lintDebug`.
5. Check Home, a coin result, Combine, Custom, Settings, Language, About and Tip jar on a 480×800 screen.
   Check long labels, font scaling, right-to-left layouts and script rendering on a physical Kompakt.

Most folders use a language code. Android uses `values-iw` for Hebrew, `values-in` for Indonesian,
`values-b+fil` for Filipino and `values-b+zh+Hans` / `values-b+zh+Hant` for Chinese scripts.
The app name and die codes (D4–D20) are not translated.

## Review status

The initial translations were generated with AI. Completeness, XML and placeholder checks pass;
lint checks plural coverage. These checks do not establish linguistic quality.
Native-speaker review is still needed. The separate visual audit of all 63 languages was not available
at the import checkpoint; incorporate its corrections before the first release.

For corrections, include the language, screen, current text and proposed wording in an issue or pull request.
The English privacy policy and terms in `docs/` are the reference; in-app translations are provided for convenience.
