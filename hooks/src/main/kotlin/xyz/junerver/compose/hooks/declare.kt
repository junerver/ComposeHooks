@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest

/** 更符合 Compose 的函数命名方式 */

@Composable
inline fun <reified TData : Any> rememberRequest(
    noinline requestFn: SuspendNormalFunction<TData>,
    options: RequestOptions<TData> = defaultOption(),
    plugins: Array<@Composable (RequestOptions<TData>) -> Plugin<TData>> = emptyArray(),
) = useRequest(requestFn, options, plugins)

@Composable
fun rememberBoolean(default: Boolean = false) = useBoolean(default)

@Composable
fun <T> rememberCreation(vararg keys: Any?, factory: () -> T) =
    useCreation(*keys, factory = factory)

@Composable
fun <S> rememberDebounce(
    value: S,
    options: DebounceOptions = defaultOption(),
) = useDebounce(value, options)

@Composable
fun rememberDebounceFn(
    fn: VoidFunction,
    options: DebounceOptions = defaultOption(),
) = useDebounceFn(fn, options)

@Composable
fun LaunchedDebounceEffect(
    vararg keys: Any?,
    options: DebounceOptions = defaultOption(),
    block: SuspendAsyncFn,
) = useDebounceEffect(*keys, options = options, block = block)

@Composable
fun rememberInterval(
    options: IntervalOptions = defaultOption(),
    fn: () -> Unit,
) = useInterval(options, fn)

@Composable
fun <T> rememberLatestRef(value: T) = useLatestRef(value)

@Composable
fun rememberMount(fn: () -> Unit) = useMount(fn)

@Composable
fun <T> rememberPrevious(present: T) = usePrevious(present)

@Composable
fun <S, A> rememberReducer(reducer: Reducer<S, A>, initialState: S) =
    useReducer(reducer, initialState)

@Composable
fun <T> rememberRef(default: T) = useRef(default)

@Composable
fun <T> rememberState(default: T) = _useState(default)

@Composable
fun <S> rememberThrottle(value: S, options: ThrottleOptions = defaultOption()) =
    useThrottle(value, options)

@Composable
fun rememberThrottleFn(
    fn: VoidFunction,
    options: ThrottleOptions = defaultOption(),
) = useThrottleFn(fn, options)

@Composable
fun LaunchedThrottleEffect(
    vararg keys: Any?,
    options: ThrottleOptions = defaultOption(),
    block: SuspendAsyncFn,
) = useThrottleEffect(*keys, options = options, block = block)

@Composable
fun rememberTimeout(delay: Duration = 1.seconds, fn: () -> Unit) = useTimeout(delay, fn)

@Composable
fun <T> rememberToggle(
    defaultValue: T? = null,
    reverseValue: T? = null,
) = useToggle(defaultValue, reverseValue)

@Composable
fun <L, R> rememberToggleEither(
    defaultValue: L? = null,
    reverseValue: R? = null,
) = useToggleEither(defaultValue, reverseValue)

@Composable
fun rememberToggleVisible(
    isVisible: Boolean = false,
    content: @Composable () -> Unit,
) = useToggleVisible(isVisible, content)

@Composable
fun rememberToggleVisible(
    isFirst: Boolean = true,
    content1: @Composable () -> Unit,
    content2: @Composable () -> Unit,
) = useToggleVisible(isFirst, content1, content2)

@Composable
fun <T> rememberUndo(initialPresent: T) = useUndo(initialPresent)

@Composable
fun rememberUnmount(fn: () -> Unit) = useUnmount(fn)
