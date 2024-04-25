@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.compose.runtime.Composable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.annotations.Nullable
import xyz.junerver.compose.hooks.useredux.DispatchAsync
import xyz.junerver.compose.hooks.useredux.DispatchCallback
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.kotlin.Tuple2

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
    options: RequestOptions<TData> = defaultOption(),
    plugins: Array<@Composable (RequestOptions<TData>) -> Plugin<TData>> = emptyArray(),
) = useRequest(requestFn, options, plugins)

//region useAsync
@Composable
fun rememberAsync(fn: SuspendAsyncFn) = useAsync(fn)

@Composable
fun rememberAsync(): AsyncRunFn = useAsync()
//endregion

@Composable
fun rememberBackToFrontEffect(vararg keys: Any?, effect: () -> Unit) =
    useBackToFrontEffect(*keys, effect = effect)

@Composable
fun rememberFrontToBackEffect(vararg keys: Any?, effect: () -> Unit) =
    useFrontToBackEffect(*keys, effect = effect)

@Composable
fun rememberBoolean(default: Boolean = false) = useBoolean(default)

@Composable
fun rememberClipboard(): Tuple2<CopyFn, PasteFn> = useClipboard()

@Composable
fun <T> rememberContext(context: ReactContext<T>) = useContext(context)

@Composable
fun <T> rememberCreation(vararg keys: Any?, factory: () -> T) =
    useCreation(*keys, factory = factory)

//region useDebounce
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
//endregion

//region useEvent
@Composable
inline fun <reified T : Any> rememberEventSubscribe(noinline subscriber: (T) -> Unit) =
    useEventSubscribe(subscriber)

@Composable
inline fun <reified T : Any> rememberEventPublish(): (T) -> Unit = useEventPublish()
//endregion

@Composable
fun rememberInterval(
    options: IntervalOptions = defaultOption(),
    fn: () -> Unit,
) = useInterval(options, fn)

@Composable
fun rememberKeyboard() = useKeyboard()

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
fun rememberNow(options: UseNowOptions = defaultOption()) = useNow(options)

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
fun rememberSensor(
    sensorType: Int,
    onAccuracyChangedFn: (Sensor, Int) -> Unit = { _, _ -> },
    onSensorChangedFn: (SensorEvent) -> Unit = { _ -> },
) = useSensor(sensorType, onAccuracyChangedFn, onSensorChangedFn)

@Composable
fun <T> rememberState(default: T & Any) = useState(default)

@Composable
fun <T> _rememberState(@Nullable default: T) = _useState(default)

//region useThrottle
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
//endregion

@Composable
fun rememberTimeout(delay: Duration = 1.seconds, fn: () -> Unit) = useTimeout(delay, fn)

@Composable
fun rememberTimestamp(
    options: TimestampOptions = defaultOption(),
) = useTimestamp(options)

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
fun rememberUnmount(fn: () -> Unit) = useUnmount(fn)

@Composable
fun rememberUpdate(): () -> Unit = useUpdate()

@Composable
fun rememberUpdateEffect(vararg keys: Any?, block: SuspendAsyncFn) =
    useUpdateEffect(*keys, block = block)
