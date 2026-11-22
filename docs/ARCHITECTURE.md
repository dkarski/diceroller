# Architecture and behavior

Kotlin, Jetpack Compose and Mudita Mindful Design (MMD), with one Activity and enum-based navigation.
No network permission, backend, analytics or session database.

`data/RollSession.kt` is pure Kotlin: each die keeps its type, range and value. Totals use Long.
Random values and monotonic times can be injected into tests. A superseded expiry cannot clear a newer result.

`ui/App.kt` owns navigation, saved view state, the five-second result timeout and keep-screen-on behavior.
`DiceScreen.kt` displays the grid and fixed Combine bar. `CustomDieScreen.kt` edits 2–9999 sides without rolling;
older combined entries retain their original ranges. The icon edits and the separate Custom label rolls.

Leaving Home hides the latest result while retaining a combined set. Reroll repeats the existing specifications;
Done or system Back ends the set. Coin contributes 1 for Heads or 2 for Tails, regardless of its display preference.

`SettingsStore` saves language, vibration, theme, keep-screen-on and coin presentation. Roll session state uses
Compose saved state, not persistent preferences: navigation and Activity recreation can restore it, while a fresh
launch after force-stop starts empty. Android task restoration after process reclamation may restore view state.

## E Ink

Use black and white, no transitions, ripples, loading animation or blinking cursor. Prefer MMD components.
Dark theme swaps the E Ink scheme; the MuditaOS status bar remains system-controlled.
Keep screen on uses a foreground window flag and releases it after ten minutes without an accepted action.

Editable graphics are in `assets/dice/`. Run `python3 scripts/import-dice-icons.py` to generate VectorDrawables.
No runtime SVG library is needed. D20 uses a lighter inner stroke.

## Device acceptance

On a physical Kompakt, check rapid rolls, 2d6+1d8 and reroll, mixed Custom ranges, coin presentation,
navigation, Activity recreation, screen sleep, theme persistence and increased system font size.
Assess icon edges, haptics and ghosting physically; screenshots cannot establish those qualities.

Separate history, alternate dice skins and durable session persistence are outside 1.0.
