package com.truffleapp.truffle.ui.components

import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Immutable
class TruffleHaptics internal constructor(
    private val vibrator: Vibrator?,
) {
    fun tick() {
        play(VibrationEffect.EFFECT_TICK)
    }

    fun click() {
        play(VibrationEffect.EFFECT_CLICK)
    }

    fun heavyClick() {
        play(VibrationEffect.EFFECT_HEAVY_CLICK)
    }

    private fun play(effectId: Int) {
        vibrator?.vibrate(VibrationEffect.createPredefined(effectId))
    }
}

@Composable
fun rememberHaptics(): TruffleHaptics {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val vibrator = context
            .getSystemService(VibratorManager::class.java)
            ?.defaultVibrator
        TruffleHaptics(vibrator)
    }
}
