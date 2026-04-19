package com.truffleapp.truffle.ui.screens

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.R
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorPage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashTitleFont = FontFamily(
    Font(
        resId     = R.font.cormorant_garamond_semibold_italic,
        weight    = FontWeight.SemiBold,
        style     = FontStyle.Italic,
    ),
)

@Composable
fun TruffleSplash(
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    val reduceMotion = remember(context) { context.prefersReducedMotion() }

    val scale     = remember { Animatable(if (reduceMotion) 1f else 0.92f) }
    val textAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val rootAlpha = remember { Animatable(1f) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            delay(200)
            rootAlpha.animateTo(0f, tween(durationMillis = 180, easing = FastOutSlowInEasing))
            onFinished()
            return@LaunchedEffect
        }
        coroutineScope {
            launch {
                scale.animateTo(
                    targetValue      = 1f,
                    animationSpec    = tween(durationMillis = 680, easing = FastOutSlowInEasing),
                )
            }
            launch {
                textAlpha.animateTo(
                    targetValue      = 1f,
                    animationSpec    = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                )
            }
        }
        delay(140)
        rootAlpha.animateTo(0f, tween(durationMillis = 220, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPage)
            .graphicsLayer { alpha = rootAlpha.value },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "truffle",
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                alpha = textAlpha.value
            },
            style = TextStyle(
                fontFamily = SplashTitleFont,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                fontSize   = 44.sp,
                color      = ColorInk,
                letterSpacing = 0.sp,
            ),
        )
    }
}

private fun Context.prefersReducedMotion(): Boolean {
    val cr = contentResolver
    val anim = Settings.Global.getFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    val trans = Settings.Global.getFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
    return anim == 0f || trans == 0f
}
