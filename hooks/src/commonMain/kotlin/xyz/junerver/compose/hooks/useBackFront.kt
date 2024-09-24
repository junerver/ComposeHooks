package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleResumeEffect
import xyz.junerver.compose.hooks.utils.unwrap

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-15:10
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Description: 进入后台**再次**回到前台时执行Effect，不同于官方 API，只在再进入时才执行。 第一次渲染（不执行）->
 * 进入后台在返回（执行）。
 *
 * Effect is executed when entering the background and returning to the
 * foreground. Different from the official API, it is only executed when
 * re-entering.
 *
 * First rendering (not executed) -> enter the background and return
 * (executed）
 */

@Composable
fun useBackToFrontEffect(vararg deps: Any?, effect: () -> Unit) {
    var inBackgroundRef by useRef(default = false)
    LifecycleResumeEffect(keys = unwrap(deps)) {
        if (inBackgroundRef) {
            effect()
            inBackgroundRef = false
        }
        onPauseOrDispose { inBackgroundRef = true }
    }
}

/**
 * Use front to back effect,contrary to the [useBackToFrontEffect]
 * behavior, the effect is executed when the App enters the background
 *
 * @param deps
 * @param effect
 * @receiver
 */

@Composable
fun useFrontToBackEffect(vararg deps: Any?, effect: () -> Unit) {
    var inBackgroundRef by useRef(default = false)
    LifecycleResumeEffect(keys = unwrap(deps)) {
        if (inBackgroundRef) {
            inBackgroundRef = false
        }
        onPauseOrDispose {
            effect()
            inBackgroundRef = true
        }
    }
}
