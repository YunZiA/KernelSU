package me.weishu.kernelsu.ui.component.navigation

import android.util.Log
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.scope.AnimatedDestinationScope
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import me.weishu.kernelsu.ui.component.getCornerRadiusTop
import me.weishu.kernelsu.ui.component.navigation.MiuixNavHostDefaults.NavAnimationEasing
import me.weishu.kernelsu.ui.component.navigation.MiuixNavHostDefaults.SHARETRANSITION_DURATION

fun <T> ManualComposableCallsBuilder.miuixComposable(
    destination: TypedDestinationSpec<T>,
    animation: DestinationStyle.Animated? = null,
    content: @Composable AnimatedDestinationScope<T>.() -> Unit
) {
    animation?.let {
        destination.animateWith(it)
    }
    composable(destination){
        val desTransition = this.transition
        val currentState = desTransition.currentState
        val targetState = desTransition.targetState
        val isPop = routePopupState[destination.route] == true

        with(desTransition){
            val dim = animateColor({ tween(SHARETRANSITION_DURATION, 0, NavAnimationEasing) }) { enterExitState ->
                when(enterExitState){
                    EnterExitState.Visible ->{
                        Color.Transparent
                    }
                    else -> {
                        if (isPop) {
                            colorScheme.windowDimming
                        } else {
                            Color.Transparent
                        }
                    }
                }

            }
            val screenCornerRadius = getCornerRadiusTop()
            val screenRadius = remember { mutableStateOf(0.dp) }
            LaunchedEffect(currentState,targetState) {
                screenRadius.value = when(currentState) {
                    EnterExitState.Visible -> {
                        delay(50)
                        0.dp
                    }
                    else -> {
                        if (!isPop) {
                            screenCornerRadius
                        } else {
                            0.dp
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.clip(ContinuousRoundedRectangle(screenRadius.value))
            ) {
                CompositionLocalProvider(
                    LocalAnimatedVisibilityScope provides this@composable,
                    localPopState provides isPop
                ) {
                    this@composable.content()
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(dim.value)
                )

            }
        }


    }
}
