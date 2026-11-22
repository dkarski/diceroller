package dev.dkarski.diceroller.ui

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Touch feedback for app interactions through the view's haptic feedback: no permission needed, and the phone's
 * touch vibration setting is respected (off there means no vibration here). The system maps the constants to the
 * device's effects; on the Kompakt a tap is a light click and a hold step a heavy click. Callers must check the app vibration preference.
 */
enum class Haptic(val constant: Int) {
    /** A short tap. */
    Tap(HapticFeedbackConstants.KEYBOARD_TAP),

    /** A repeated action while holding a button. */
    Step(HapticFeedbackConstants.LONG_PRESS),
}

fun View.vibrate(haptic: Haptic) {
    performHapticFeedback(haptic.constant)
}
