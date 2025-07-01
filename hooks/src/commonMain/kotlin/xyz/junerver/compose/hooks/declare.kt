@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.PersistentList
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest

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
inline fun <reified T> rememberSelector(alias: String? = null): State<T> = useSelector(alias)

@Composable
inline fun <reified T, R> rememberSelector(alias: String? = null, crossinline block: T.() -> R) = useSelector(alias, block)
//endregion

@Composable
fun <TData : Any> rememberRequest(
    requestFn: SuspendNormalFunction<TData>,
    optionsOf: RequestOptions<TData>.() -> Unit = {},
    plugins: Array<@Composable (RequestOptions<TData>) -> Plugin<TData>> = emptyArray(),
) = useRequest(requestFn, optionsOf, plugins)

//region useAsync
@Composable
fun rememberAsync(block: SuspendAsyncFn) = useAsync(block)

@Composable
fun rememberAsync(): AsyncRunFn = useAsync()

@Composable
fun rememberCancelableAsync(): CancelableAsyncHolder = useCancelableAsync()
//endregion

@Composable
fun <T> rememberAutoReset(default: T & Any, interval: Duration) = useAutoReset(default, interval)

@Composable
fun rememberBackToFrontEffect(vararg keys: Any?, effect: () -> Unit) = useBackToFrontEffect(*keys, effect = effect)

@Composable
fun rememberFrontToBackEffect(vararg keys: Any?, effect: () -> Unit) = useFrontToBackEffect(*keys, effect = effect)

@Composable
fun rememberBoolean(default: Boolean = false) = useBoolean(default)

@Composable
fun rememberClipboard() = useClipboard()

@Composable
fun <T> rememberContext(context: ReactContext<T>) = useContext(context)

@Composable
fun rememberCountdown(optionsOf: CountdownOptions.() -> Unit) = useCountdown(optionsOf)

@Composable
fun rememberCounter(initialValue: Int = 0, optionsOf: CounterOptions.() -> Unit) = useCounter(initialValue, optionsOf)

@Composable
fun <T> rememberCreation(vararg keys: Any?, factory: () -> T) = useCreation(*keys, factory = factory)

//region useDebounce
@Composable
fun <S> rememberDebounce(value: S, optionsOf: DebounceOptions.() -> Unit = {}) = useDebounce(value, optionsOf)

@Composable
fun rememberDebounceFn(fn: VoidFunction, optionsOf: DebounceOptions.() -> Unit = {}) = useDebounceFn(fn, optionsOf)

@Composable
fun LaunchedDebounceEffect(vararg keys: Any?, optionsOf: DebounceOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useDebounceEffect(*keys, optionsOf = optionsOf, block = block)
//endregion

//region useEvent
@Composable
inline fun <reified T : Any> rememberEventSubscribe(noinline subscriber: (T) -> Unit) = useEventSubscribe(subscriber)

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
fun rememberInterval(optionsOf: IntervalOptions.() -> Unit = {}, block: SuspendAsyncFn) = useInterval(optionsOf, block)

@Composable
fun rememberInterval(optionsOf: IntervalOptions.() -> Unit = {}, ready: Boolean, block: SuspendAsyncFn) =
    useInterval(optionsOf, ready, block)

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
fun rememberMount(block: SuspendAsyncFn) = useMount(block)

@Composable
fun rememberNow(optionsOf: UseNowOptions.() -> Unit = {}) = useNow(optionsOf)

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
fun <S, A> rememberReducer(reducer: Reducer<S, A>, initialState: S, middlewares: Array<Middleware<S, A>> = emptyArray()) =
    useReducer(reducer, initialState, middlewares)

@Composable
fun <T> rememberRef(default: T) = useRef(default)

@Composable
fun <T> rememberResetState(default: T & Any) = useResetState(default)

@Composable
fun <T> rememberState(default: T & Any) = useState(default)

@Composable
fun <T> _rememberState(default: T) = _useState(default)

//region useThrottle
@Composable
fun <S> rememberThrottle(value: S, optionsOf: ThrottleOptions.() -> Unit = {}) = useThrottle(value, optionsOf)

@Composable
fun rememberThrottleFn(fn: VoidFunction, optionsOf: ThrottleOptions.() -> Unit = {}) = useThrottleFn(fn, optionsOf)

@Composable
fun LaunchedThrottleEffect(vararg keys: Any?, optionsOf: ThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useThrottleEffect(*keys, optionsOf = optionsOf, block = block)
//endregion

@Composable
fun rememberTimeout(delay: Duration = 1.seconds, block: () -> Unit) = useTimeout(delay, block)

@Composable
fun rememberTimestamp(optionsOf: TimestampOptions.() -> Unit = {}, autoResume: Boolean = true) = useTimestamp(optionsOf, autoResume)

@Composable
fun rememberTimestampRef(optionsOf: TimestampOptions.() -> Unit = {}, autoResume: Boolean = true) = useTimestampRef(optionsOf, autoResume)

//region useToggle
@Composable
fun <T> rememberToggle(defaultValue: T? = null, reverseValue: T? = null) = useToggle(defaultValue, reverseValue)

@Composable
fun <L, R> rememberToggleEither(defaultValue: L? = null, reverseValue: R? = null) = useToggleEither(defaultValue, reverseValue)

@Composable
fun rememberToggleVisible(isVisible: Boolean = false, content: @Composable () -> Unit) = useToggleVisible(isVisible, content)

@Composable
fun rememberToggleVisible(isFirst: Boolean = true, content1: @Composable () -> Unit, content2: @Composable () -> Unit) =
    useToggleVisible(isFirst, content1, content2)
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
fun rememberUpdateEffect(vararg keys: Any?, block: SuspendAsyncFn) = useUpdateEffect(*keys, block = block)

@Composable
fun rememberLastChanged(source: Any?): State<Instant> = useLastChanged(source)

@Composable
fun <KEY, ITEM> rememberSelectable(
    selectionMode: SelectionMode<KEY>,
    items: List<ITEM>,
    keyProvider: (ITEM) -> KEY,
): SelectableHolder<KEY, ITEM> = useSelectable(selectionMode, items, keyProvider)

@Composable
fun <S : Any, E, CTX> rememberStateMachine(machineGraph: Ref<MachineGraph<S, E, CTX>>): StateMachineHolder<S, E, CTX> =
    useStateMachine(machineGraph)

@Composable
fun rememberTimeoutFn(fn: SuspendAsyncFn, interval: Duration = 1.seconds, optionsOf: TimeoutFnOptions.() -> Unit = {}): TimeoutFnHolder =
    useTimeoutFn(fn, interval, optionsOf)

@Composable
fun rememberTimeoutPoll(
    fn: SuspendAsyncFn,
    interval: Duration = 1.seconds,
    optionsOf: UseTimeoutPollOptions.() -> Unit = {},
): TimeoutPollHolder = useTimeoutPoll(fn, interval, optionsOf)

@Composable
fun rememberTimeoutPoll(fn: SuspendAsyncFn, interval: Duration = 1.seconds, immediate: Boolean = true) =
    useTimeoutPoll(fn, interval, immediate)

@Composable
fun <T> rememberImmutableList(vararg elements: T): ImmutableListHolder<T> = useImmutableList(*elements)

@Composable
fun <S, T : S> rememberImmutableListReduce(list: PersistentList<T>, operation: (acc: S, T) -> S): State<S> =
    useImmutableListReduce(list, operation)
