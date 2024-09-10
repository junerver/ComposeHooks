@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import arrow.core.Either
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.useredux.DispatchCallback
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.kotlin.Tuple4
import xyz.junerver.kotlin.Tuple5

/** 更符合 Compose 的函数命名方式 */

//region useRedux
@Composable
inline fun <reified A> rememberDispatch(alias: String? = null): Dispatch<A> = useDispatch(alias)

@Composable
inline fun <reified A> rememberDispatchAsync(
    alias: String? = null,
    noinline onBefore: DispatchCallback<A>? = null,
    noinline onFinally: DispatchCallback<A>? = null,
): DispatchAsync<A> = useDispatchAsync(alias, onBefore, onFinally)

@Composable
inline fun <reified T> rememberSelector(alias: String? = null): T = useSelector(alias)

@Composable
inline fun <reified T, R> rememberSelector(alias: String? = null, block: T.() -> R) =
    useSelector(alias, block)
//endregion

@Composable
fun <TData : Any> rememberRequest(
    requestFn: SuspendNormalFunction<TData>,
    options: RequestOptions<TData> = RequestOptions(),
    plugins: Array<@Composable (RequestOptions<TData>) -> Plugin<TData>> = emptyArray(),
) = useRequest(requestFn, options, plugins)

//region useAsync
@Composable
fun rememberAsync(fn: SuspendAsyncFn) = useAsync(fn)

@Composable
fun rememberAsync(): AsyncRunFn = useAsync()
//endregion

@Composable
fun <T> rememberAutoReset(default: T & Any, interval: Duration) = useAutoReset(default, interval)

@Composable
fun rememberBackToFrontEffect(vararg keys: Any?, effect: () -> Unit) =
    useBackToFrontEffect(*keys, effect = effect)

@Composable
fun rememberFrontToBackEffect(vararg keys: Any?, effect: () -> Unit) =
    useFrontToBackEffect(*keys, effect = effect)

@Composable
fun rememberBoolean(default: Boolean = false) = useBoolean(default)

@Composable
fun rememberClipboard(): Pair<CopyFn, PasteFn> = useClipboard()

@Composable
fun <T> rememberContext(context: ReactContext<T>) = useContext(context)

@Composable
fun rememberCountdown(options: CountdownOptions): Pair<Duration, FormattedRes> =
    useCountdown(options)

@Composable
fun rememberCounter(
    initialValue: Int = 0,
    options: CounterOptions,
): Tuple5<Int, IncFn, DecFn, SetValueFn<Either<Int, (Int) -> Int>>, ResetFn> =
    useCounter(initialValue, options)

@Composable
fun <T> rememberCreation(vararg keys: Any?, factory: () -> T) =
    useCreation(*keys, factory = factory)

//region useDebounce
@Composable
fun <S> rememberDebounce(
    value: S,
    options: DebounceOptions = DebounceOptions(),
) = useDebounce(value, options)

@Composable
fun rememberDebounceFn(
    fn: VoidFunction,
    options: DebounceOptions = DebounceOptions(),
) = useDebounceFn(fn, options)

@Composable
fun LaunchedDebounceEffect(
    vararg keys: Any?,
    options: DebounceOptions = DebounceOptions(),
    block: SuspendAsyncFn,
) = useDebounceEffect(*keys, options = options, block = block)
//endregion

//region useEvent
@Composable
inline fun <reified T : Any> rememberEventSubscribe(noinline subscriber: (T) -> Unit) =
    useEventSubscribe(subscriber)

@Composable
inline fun <reified T : Any> rememberEventPublish(): (T) -> Unit = useEventPublish()
//endregion

@Composable
fun rememberKeyboard() = useKeyboard()

//region useGetState
@Composable
fun <T> rememberGetState(default: T & Any) = useGetState(default = default)

@Composable
fun <T> _rememberGetState(default: T) = _useGetState(default = default)

//endregion

@Composable
fun rememberInterval(
    options: IntervalOptions = IntervalOptions(),
    block: () -> Unit,
) = useInterval(options, block)

@Composable
fun rememberInterval(
    options: IntervalOptions = IntervalOptions(),
    ready: Boolean,
    block: () -> Unit,
) = useInterval(options, ready, block)

@Composable
fun <T> rememberLatestRef(value: T) = useLatestRef(value)

//region useList
@Composable
fun <T> rememberList(elements: Collection<T>) = useList(elements)

@Composable
fun <T> rememberList(vararg elements: T) = useList(*elements)
//endregion

//region useMap
@Composable
fun <K, V> rememberMap(vararg pairs: Pair<K, V>) = useMap(*pairs)

@Composable
fun <K, V> rememberMap(pairs: Iterable<Pair<K, V>>) = useMap(pairs)
//endregion

@Composable
fun rememberMount(fn: SuspendAsyncFn) = useMount(fn)

@Composable
fun rememberNow(options: UseNowOptions = UseNowOptions()) = useNow(options)

//region useNumber
@Composable
fun rememberDouble(default: Double = 0.0) = useDouble(default)

@Composable
fun rememberFloat(default: Float = 0f) = useFloat(default)

@Composable
fun rememberInt(default: Int = 0) = useInt(default)

@Composable
fun rememberLong(default: Long = 0L) = useLong(default)
//endregion

@Composable
fun <T> rememberPersistent(key: String, defaultValue: T) = usePersistent(key, defaultValue)

@Composable
fun <T> rememberPrevious(present: T) = usePrevious(present)

@Composable
fun <S, A> rememberReducer(
    reducer: Reducer<S, A>,
    initialState: S,
    middlewares: Array<Middleware<S, A>> = emptyArray(),
) =
    useReducer(reducer, initialState, middlewares)

@Composable
fun <T> rememberRef(default: T) = useRef(default)

@Composable
fun <T> rememberResetState(default: T & Any): Tuple4<T, SetValueFn<T & Any>, GetValueFn<T>, ResetFn> =
    useResetState(default)

@Composable
fun <T> rememberState(default: T & Any) = useState(default)

@Composable
fun <T> _rememberState(default: T) = _useState(default)

//region useThrottle
@Composable
fun <S> rememberThrottle(value: S, options: ThrottleOptions = ThrottleOptions()) =
    useThrottle(value, options)

@Composable
fun rememberThrottleFn(
    fn: VoidFunction,
    options: ThrottleOptions = ThrottleOptions(),
) = useThrottleFn(fn, options)

@Composable
fun LaunchedThrottleEffect(
    vararg keys: Any?,
    options: ThrottleOptions = ThrottleOptions(),
    block: SuspendAsyncFn,
) = useThrottleEffect(*keys, options = options, block = block)
//endregion

@Composable
fun rememberTimeout(delay: Duration = 1.seconds, block: () -> Unit) = useTimeout(delay, block)

@Composable
fun rememberTimestamp(
    options: TimestampOptions = TimestampOptions(),
    autoResume: Boolean = true,
) = useTimestamp(options, autoResume)

@Composable
fun rememberTimestampRef(
    options: TimestampOptions = TimestampOptions(),
    autoResume: Boolean = true,
) = useTimestampRef(options, autoResume)

//region useToggle
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
//endregion

@Composable
fun <T> rememberUndo(initialPresent: T) = useUndo(initialPresent)

@Composable
fun rememberUnmount(block: () -> Unit) = useUnmount(block)

@Composable
fun rememberUnmountedRef() = useUnmountedRef()

@Composable
fun rememberUpdate(): () -> Unit = useUpdate()

@Composable
fun rememberUpdateEffect(vararg keys: Any?, block: SuspendAsyncFn) =
    useUpdateEffect(*keys, block = block)
