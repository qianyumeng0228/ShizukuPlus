package af.shizuku.manager.utils

import af.shizuku.manager.ShizukuSettings
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Utility for providing "expressive" haptic feedback,
 * common in modern Material 3 Enhanced (M3E) applications.
 */
object HapticUtils {

    // Some OEM skins (confirmed on HyperOS/MIUI - #SHIZUKUPLUS-8E) route
    // View.performHapticFeedback through their own vibrator stack instead of the
    // normally VIBRATE-exempt system haptic path, and throw a SecurityException
    // if the VIBRATE permission isn't held. Haptics are decorative, never worth
    // crashing over, so every call goes through this guard.
    //
    // The dedicated haptic-feedback setting is checked once here so every call
    // site is governed by a single flag, instead of ad-hoc per-call-site checks
    // of unrelated toggles (e.g. expressive animations).
    private inline fun safeHaptic(view: View, constant: Int) {
        if (!ShizukuSettings.isHapticFeedbackEnabled()) return
        try {
            view.performHapticFeedback(constant)
        } catch (e: SecurityException) {
            // Swallow - see comment above.
        }
    }

    /**
     * Standard click feedback (vibration)
     */
    fun tap(view: View) {
        safeHaptic(view, HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /**
     * "Impact" feedback for success actions
     */
    fun success(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            safeHaptic(view, HapticFeedbackConstants.CONFIRM)
        } else {
            safeHaptic(view, HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * "Impact" feedback for error/warning actions
     */
    fun error(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            safeHaptic(view, HapticFeedbackConstants.REJECT)
        } else {
            safeHaptic(view, HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * Subtle "tick" feedback for scrolling or minor increments
     */
    fun tick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            safeHaptic(view, HapticFeedbackConstants.CLOCK_TICK)
        } else {
            safeHaptic(view, HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    /**
     * Feedback for the start of a gesture (e.g. drag start)
     */
    fun gestureStart(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            safeHaptic(view, HapticFeedbackConstants.GESTURE_START)
        } else {
            safeHaptic(view, HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * Feedback for reaching a threshold during a gesture
     */
    fun gestureThreshold(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            safeHaptic(view, HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE)
        } else {
            tick(view)
        }
    }

    /**
     * Heavy "thud" feedback for significant UI events
     */
    fun heavyClick(view: View) {
        safeHaptic(view, HapticFeedbackConstants.LONG_PRESS)
    }
}
