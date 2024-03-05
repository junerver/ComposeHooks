package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState

/**
 * Description: 返回当前最新值的 Hook，可以避免在使用解构写法时的闭包问题。
 * compose 库中有官方类似实现：[rememberUpdatedState]。
 * 但是这个实现的返回值是状态，而非ref，如果直接操作会造成重组。
 *
 * 简单说如果你只是需要props的最新值，用来避免闭包问题，比不需要操作、计算、缓存、传递，完全不需要使用这个钩子，
 * 直接使用官方实现：[rememberUpdatedState]。
 * @author Junerver
 * date: 2024/2/21-8:45
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun <T> useLatestRef(value: T): Ref<T> = useRef(default = value).apply { current = value }

@Composable
fun <T> useLatestState(value: T) = rememberUpdatedState(value)
