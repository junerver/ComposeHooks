package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import arrow.core.Either
import arrow.core.left
import arrow.core.right

/**
 * 用于在两个状态值间切换的 Hook。
 */
@Composable
fun <T> useToggle(
    defaultValue: T? = null,
    reverseValue: T? = null,
): Pair<T?, () -> Unit> {
    // 默认是左值（false）
    val (isLeft, toggle) = useBoolean(true)
    return (if (isLeft) defaultValue else reverseValue) to toggle
}

/**
 * Description:用于在两个状态值间切换的 Hook。为了保证类型信息不消失，使用[Either]作为容器保存；
 * 调用者在使用时根据实际情况调用 [Either.fold] 函数或者其他函数处理；
 */
@Composable
fun <L, R> useToggleEither(
    defaultValue: L? = null,
    reverseValue: R? = null,
): Pair<Either<L?, R?>, () -> Unit> {
    // 默认是左值
    val (isLeft, toggle) = useBoolean(true)
    return (if (isLeft) defaultValue.left() else reverseValue.right()) to toggle
}

/**
 * 用于方便的切换控制组件的可见性
 */
@Composable
fun useToggleVisible(
    isVisible: Boolean = false,
    content: @Composable () -> Unit,
): Pair<@Composable () -> Unit, () -> Unit> {
    val empty: @Composable () -> Unit = {}
    return useToggleVisible(isVisible, content, empty)
}

@Composable
fun useToggleVisible(
    isFirst: Boolean = true,
    content1: @Composable () -> Unit,
    content2: @Composable () -> Unit,
): Pair<@Composable () -> Unit, () -> Unit> {
    val (visible, toggle) = useBoolean(isFirst)
    return (if (visible) content1 else content2) to toggle
}
