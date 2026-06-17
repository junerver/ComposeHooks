@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.collections.immutable.PersistentList
import kotlinx.datetime.LocalDateTime
import xyz.junerver.compose.hooks.annotation.ExperimentalComputed
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.usses.SseHolder
import xyz.junerver.compose.hooks.usses.SseStreamFn
import xyz.junerver.compose.hooks.usses.UseSseOptions
import xyz.junerver.compose.hooks.usses.useSse
import xyz.junerver.compose.hooks.usetable.TableHolder
import xyz.junerver.compose.hooks.usetable.TableOptions
import xyz.junerver.compose.hooks.usetable.core.ColumnDef
import xyz.junerver.compose.hooks.usetable.useTable
import xyz.junerver.compose.hooks.utils.currentInstant

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
fun <TParams, TData : Any> rememberRequest(
    requestFn: SuspendNormalFunction<TParams?, TData>,
    optionsOf: UseRequestOptions<TParams, TData>.() -> Unit = {},
    plugins: Array<@Composable (UseRequestOptions<TParams, TData>) -> Plugin<TParams, TData>> = emptyArray(),
) = useRequest(requestFn, optionsOf, plugins)

@Composable
fun <TParams, TEvent> rememberSse(
    streamFn: SseStreamFn<TParams, TEvent>,
    optionsOf: UseSseOptions<TParams, TEvent>.() -> Unit = {},
): SseHolder<TParams, TEvent> = useSse(streamFn, optionsOf)

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
fun rememberEffect(vararg deps: Any?, block: SuspendAsyncFn) = useEffect(*deps, block = block)

@Composable
fun rememberFrontToBackEffect(vararg keys: Any?, effect: () -> Unit) = useFrontToBackEffect(*keys, effect = effect)

@Composable
fun rememberBoolean(default: Boolean = false) = useBoolean(default)

@Composable
fun rememberClipboard() = useClipboard()

@Composable
fun <T> rememberControllable(default: T & Any) = useControllable(default)

@Composable
fun <T> _rememberControllable(default: T) = _useControllable(default)

@Composable
fun <T> rememberContext(context: ReactContext<T>) = useContext(context)

@Composable
fun rememberCountdown(optionsOf: UseCountdownOptions.() -> Unit) = useCountdown(optionsOf)

@Composable
fun rememberCounter(initialValue: Int = 0, optionsOf: UseCounterOptions.() -> Unit) = useCounter(initialValue, optionsOf)

@Composable
fun <T> rememberCreation(vararg keys: Any?, factory: () -> T) = useCreation(*keys, factory = factory)

@Composable
fun <T> rememberCycleList(list: PersistentList<T>, optionsOf: UseCycleListOptions<T>.() -> Unit = {}) =
    useCycleList(list, optionsOf)

//region useDateFormat
@Composable
fun rememberDateFormat(
    date: Instant = currentInstant,
    formatStr: String = "HH:mm:ss",
    optionsOf: UseDateFormatOptions.() -> Unit = {},
): State<String> = useDateFormat(date, formatStr, optionsOf)

@Composable
fun rememberDateFormat(date: LocalDateTime, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormat(date, formatStr, optionsOf)

@Composable
fun rememberDateFormat(date: String, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormat(date, formatStr, optionsOf)

@Composable
fun rememberDateFormat(date: Long, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormat(date, formatStr, optionsOf)
//endregion

//region useDebounce
@Composable
fun <S> rememberDebounce(value: S, optionsOf: UseDebounceOptions.() -> Unit = {}) = useDebounce(value, optionsOf)

@Composable
fun <TParams> rememberDebounceFn(fn: VoidFunction<TParams>, optionsOf: UseDebounceOptions.() -> Unit = {}) = useDebounceFn(fn, optionsOf)

@Composable
fun rememberDebounceEffect(vararg keys: Any?, optionsOf: UseDebounceOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useDebounceEffect(*keys, optionsOf = optionsOf, block = block)

@Composable
fun LaunchedDebounceEffect(vararg keys: Any?, optionsOf: UseDebounceOptions.() -> Unit = {}, block: SuspendAsyncFn) =
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
fun rememberInterval(optionsOf: UseIntervalOptions.() -> Unit = {}, block: SuspendAsyncFn) = useInterval(optionsOf, block)

@Composable
fun rememberInterval(optionsOf: UseIntervalOptions.() -> Unit = {}, ready: Boolean, block: SuspendAsyncFn) =
    useInterval(optionsOf, ready, block)

@Composable
fun <T> rememberLatestRef(value: T) = useLatestRef(value)

@Composable
fun <T> rememberLatestState(value: T) = useLatestState(value)

//region useList
@Composable
fun <T> rememberList(elements: Collection<T>) = useList(elements)

@Composable
fun <T> rememberList(vararg elements: T) = useList(*elements)

@Composable
fun <S, T : S> rememberListReduce(list: List<T>, operation: (acc: S, T) -> S): State<S?> = useListReduce(list, operation)
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
fun <T, R> rememberMemoizedFn(fn: suspend DeepRecursiveScope<T, R>.(T) -> R): DeepRecursiveFunction<T, R> = useMemoizedFn(fn)

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
fun <T> rememberPersistent(key: String, defaultValue: T, forceUseMemory: Boolean = false) = usePersistent(key, defaultValue, forceUseMemory)

@Composable
fun <T> rememberPrevious(present: T) = usePrevious(present)

@Composable
fun rememberPausableEffect(vararg deps: Any?, block: SuspendAsyncFn): PausableEffectHolder =
    usePausableEffect(*deps, block = block)

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

@Composable
fun <T> rememberState(vararg keys: Any?, factory: () -> T): State<T> = useState(*keys, factory = factory)

@ExperimentalComputed
@Composable
fun <T> rememberStateAsync(
    vararg keys: Any?,
    initValue: T? = null,
    optionsOf: UseStateAsyncOptions.() -> Unit = {},
    factory: suspend () -> T,
): State<T?> = useStateAsync(*keys, initValue = initValue, optionsOf = optionsOf, factory = factory)

//region useThrottle
@Composable
fun <S> rememberThrottle(value: S, optionsOf: UseThrottleOptions.() -> Unit = {}) = useThrottle(value, optionsOf)

@Composable
fun <TParams> rememberThrottleFn(fn: VoidFunction<TParams>, optionsOf: UseThrottleOptions.() -> Unit = {}) = useThrottleFn(fn, optionsOf)

@Composable
fun rememberThrottleEffect(vararg keys: Any?, optionsOf: UseThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useThrottleEffect(*keys, optionsOf = optionsOf, block = block)

@Composable
fun LaunchedThrottleEffect(vararg keys: Any?, optionsOf: UseThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useThrottleEffect(*keys, optionsOf = optionsOf, block = block)
//endregion

@Deprecated(message = "useTimeout with delay and block is deprecated. Use rememberTimeoutFn instead.")
@Composable
fun rememberTimeout(delay: Duration = 1.seconds, block: () -> Unit) {
    useTimeoutFn(fn = { block() }, interval = delay)
}

@Composable
fun rememberTimestamp(optionsOf: UseTimestampOptions.() -> Unit = {}, autoResume: Boolean = true) = useTimestamp(optionsOf, autoResume)

@Composable
fun rememberTimestampRef(optionsOf: UseTimestampOptions.() -> Unit = {}, autoResume: Boolean = true) =
    useTimestampRef(optionsOf, autoResume)

@Composable
fun rememberTimeAgo(time: Instant, optionsOf: UseTimeAgoOptions.() -> Unit = {}): State<String> = useTimeAgo(time, optionsOf)

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
fun rememberTimeoutFn(fn: SuspendAsyncFn, interval: Duration = 1.seconds, optionsOf: UseTimeoutFnOptions.() -> Unit = {}): TimeoutFnHolder =
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

//region useSorted
@Composable
fun <T> rememberSorted(source: List<T>, compareFn: SortedCompareFn<T>): State<List<T>> = useSorted(source, compareFn)

@Composable
fun <T> rememberSorted(source: List<T>, optionsOf: UseSortedOptions<T>.() -> Unit = {}): State<List<T>> = useSorted(source, optionsOf)
//endregion

//region useTable
@Composable
fun <T> rememberTable(
    data: List<T>,
    columns: List<ColumnDef<T, *>>,
    optionsOf: TableOptions<T>.() -> Unit = {},
): TableHolder<T> = useTable(data, columns, optionsOf)
//endregion

//region useMath
@Composable
fun rememberAbs(number: Int): State<Int> = useAbs(number)

@Composable
fun rememberAbs(number: Double): State<Double> = useAbs(number)

@Composable
fun rememberAbs(number: Float): State<Float> = useAbs(number)

@Composable
fun rememberAbs(number: Long): State<Long> = useAbs(number)

@Composable
fun rememberCeil(number: Double): State<Double> = useCeil(number)

@Composable
fun rememberCeil(number: Float): State<Float> = useCeil(number)

@Composable
fun rememberCeil(number: Int): State<Int> = useCeil(number)

@Composable
fun rememberCeil(number: Long): State<Long> = useCeil(number)

@Composable
fun rememberFloor(number: Double): State<Double> = useFloor(number)

@Composable
fun rememberFloor(number: Float): State<Float> = useFloor(number)

@Composable
fun rememberFloor(number: Int): State<Int> = useFloor(number)

@Composable
fun rememberFloor(number: Long): State<Long> = useFloor(number)

@Composable
fun rememberRound(number: Double): State<Double> = useRound(number)

@Composable
fun rememberRound(number: Float): State<Float> = useRound(number)

@Composable
fun rememberRound(number: Int): State<Int> = useRound(number)

@Composable
fun rememberRound(number: Long): State<Long> = useRound(number)

@Composable
fun rememberTrunc(number: Double): State<Double> = useTrunc(number)

@Composable
fun rememberTrunc(number: Float): State<Float> = useTrunc(number)

@Composable
fun rememberTrunc(number: Int): State<Int> = useTrunc(number)

@Composable
fun rememberTrunc(number: Long): State<Long> = useTrunc(number)

@Composable
fun rememberMin(a: Int, b: Int): State<Int> = useMin(a, b)

@Composable
fun rememberMin(a: Long, b: Long): State<Long> = useMin(a, b)

@Composable
fun rememberMin(a: Float, b: Float): State<Float> = useMin(a, b)

@Composable
fun rememberMin(a: Double, b: Double): State<Double> = useMin(a, b)

@Composable
fun rememberMin(a: Int, b: Long): State<Long> = useMin(a, b)

@Composable
fun rememberMin(a: Long, b: Int): State<Long> = useMin(a, b)

@Composable
fun rememberMin(a: Float, b: Double): State<Double> = useMin(a, b)

@Composable
fun rememberMin(a: Double, b: Float): State<Double> = useMin(a, b)

@Composable
fun rememberMax(a: Int, b: Int): State<Int> = useMax(a, b)

@Composable
fun rememberMax(a: Long, b: Long): State<Long> = useMax(a, b)

@Composable
fun rememberMax(a: Float, b: Float): State<Float> = useMax(a, b)

@Composable
fun rememberMax(a: Double, b: Double): State<Double> = useMax(a, b)

@Composable
fun rememberMax(a: Int, b: Long): State<Long> = useMax(a, b)

@Composable
fun rememberMax(a: Long, b: Int): State<Long> = useMax(a, b)

@Composable
fun rememberMax(a: Float, b: Double): State<Double> = useMax(a, b)

@Composable
fun rememberMax(a: Double, b: Float): State<Double> = useMax(a, b)

@Composable
fun rememberPow(base: Double, exponent: Double): State<Double> = usePow(base, exponent)

@Composable
fun rememberPow(base: Double, exponent: Int): State<Double> = usePow(base, exponent)

@Composable
fun rememberPow(base: Double, exponent: Float): State<Double> = usePow(base, exponent)

@Composable
fun rememberPow(base: Float, exponent: Float): State<Float> = usePow(base, exponent)

@Composable
fun rememberPow(base: Float, exponent: Int): State<Float> = usePow(base, exponent)

@Composable
fun rememberPow(base: Float, exponent: Double): State<Double> = usePow(base, exponent)

@Composable
fun rememberPow(base: Int, exponent: Int): State<Double> = usePow(base, exponent)

@Composable
fun rememberPow(base: Int, exponent: Double): State<Double> = usePow(base, exponent)

@Composable
fun rememberPow(base: Int, exponent: Float): State<Double> = usePow(base, exponent)

@Composable
fun rememberPow(base: Long, exponent: Int): State<Double> = usePow(base, exponent)

@Composable
fun rememberPow(base: Long, exponent: Double): State<Double> = usePow(base, exponent)

@Composable
fun rememberPow(base: Long, exponent: Float): State<Double> = usePow(base, exponent)

@Composable
fun rememberSqrt(number: Double): State<Double> = useSqrt(number)

@Composable
fun rememberSqrt(number: Float): State<Float> = useSqrt(number)

@Composable
fun rememberSqrt(number: Int): State<Double> = useSqrt(number)

@Composable
fun rememberSqrt(number: Long): State<Double> = useSqrt(number)
//endregion
