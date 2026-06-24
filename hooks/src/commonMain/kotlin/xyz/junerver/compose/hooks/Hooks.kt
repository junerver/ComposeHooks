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
import xyz.junerver.compose.hooks.useboolean.BooleanHolder as BooleanHolderImpl
import xyz.junerver.compose.hooks.useboolean.useBooleanImpl
import xyz.junerver.compose.hooks.usecreation.useCreationImpl
import xyz.junerver.compose.hooks.useeffect.useEffectImpl
import xyz.junerver.compose.hooks.usemap.useMapImpl
import xyz.junerver.compose.hooks.usemount.useMountImpl
import xyz.junerver.compose.hooks.usenumber.useDoubleImpl
import xyz.junerver.compose.hooks.usenumber.useFloatImpl
import xyz.junerver.compose.hooks.usenumber.useIntImpl
import xyz.junerver.compose.hooks.usenumber.useLongImpl
import xyz.junerver.compose.hooks.useunmount.useUnmountImpl
import xyz.junerver.compose.hooks.useclipboard.CopyPasteHolder as CopyPasteHolderImpl
import xyz.junerver.compose.hooks.useclipboard.useClipboardImpl
import xyz.junerver.compose.hooks.usecontext.useContextImpl
import xyz.junerver.compose.hooks.useevent.useEventPublishImpl
import xyz.junerver.compose.hooks.useevent.useEventSubscribeImpl
import xyz.junerver.compose.hooks.usegetstate.GetStateHolder as GetStateHolderImpl
import xyz.junerver.compose.hooks.usegetstate._useGetStateImpl
import xyz.junerver.compose.hooks.usegetstate.useGetStateImpl
import xyz.junerver.compose.hooks.useimmutablelist.ImmutableListHolder as ImmutableListHolderImpl
import xyz.junerver.compose.hooks.useimmutablelist.useImmutableListFoldImpl
import xyz.junerver.compose.hooks.useimmutablelist.useImmutableListImpl
import xyz.junerver.compose.hooks.useimmutablelist.useImmutableListReduceImpl
import xyz.junerver.compose.hooks.useimmutablelist.useImmutableListReduceOrNullImpl
import xyz.junerver.compose.hooks.usekeyboard.KeyboardHolder as KeyboardHolderImpl
import xyz.junerver.compose.hooks.usekeyboard.useKeyboardImpl
import xyz.junerver.compose.hooks.uselatest.useLatestRefImpl
import xyz.junerver.compose.hooks.uselatest.useLatestStateImpl
import xyz.junerver.compose.hooks.uselist.useListImpl
import xyz.junerver.compose.hooks.uselist.useListReduceImpl
import xyz.junerver.compose.hooks.usemath.useAbsImpl
import xyz.junerver.compose.hooks.usemath.useCeilImpl
import xyz.junerver.compose.hooks.usemath.useFloorImpl
import xyz.junerver.compose.hooks.usemath.useMaxImpl
import xyz.junerver.compose.hooks.usemath.useMinImpl
import xyz.junerver.compose.hooks.usemath.usePowImpl
import xyz.junerver.compose.hooks.usemath.useRoundImpl
import xyz.junerver.compose.hooks.usemath.useSqrtImpl
import xyz.junerver.compose.hooks.usemath.useTruncImpl
import xyz.junerver.compose.hooks.usememoizedfn.useMemoizedFnImpl
import xyz.junerver.compose.hooks.useprevious.usePreviousImpl
import xyz.junerver.compose.hooks.useunmountedref.useUnmountedRefImpl
import xyz.junerver.compose.hooks.useupdate.useUpdateImpl
import xyz.junerver.compose.hooks.useupdateeffect.useUpdateEffectImpl
import xyz.junerver.compose.hooks.useform.Form as FormImpl
import xyz.junerver.compose.hooks.useform.FormInstance as FormInstanceImpl
import xyz.junerver.compose.hooks.useform.FormItemState as FormItemStateImpl
import xyz.junerver.compose.hooks.useform.FormScope as FormScopeImpl
import xyz.junerver.compose.hooks.useform.ValidationTrigger as ValidationTriggerImpl
import xyz.junerver.compose.hooks.useform.Validator as ValidatorImpl
import xyz.junerver.compose.hooks.useform.useForm
import xyz.junerver.compose.hooks.useform.useFormInstance
import xyz.junerver.compose.hooks.useform.useWatch
import xyz.junerver.compose.hooks.useredux.ReduxProvider
import xyz.junerver.compose.hooks.useredux.Store as StoreImpl
import xyz.junerver.compose.hooks.useredux.StoreRecord as StoreRecordImpl
import xyz.junerver.compose.hooks.useredux.StoreScope as StoreScopeImpl
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.plus
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.compose.hooks.userequest.CancelFn as CancelFnImpl
import xyz.junerver.compose.hooks.userequest.ComposablePluginGenFn as ComposablePluginGenFnImpl
import xyz.junerver.compose.hooks.userequest.Fetch as FetchImpl
import xyz.junerver.compose.hooks.userequest.FetchState as FetchStateImpl
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn as GenPluginLifecycleFnImpl
import xyz.junerver.compose.hooks.userequest.MutateFn as MutateFnImpl
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn as OnBeforeReturnImpl
import xyz.junerver.compose.hooks.userequest.OnRequestReturn as OnRequestReturnImpl
import xyz.junerver.compose.hooks.userequest.Plugin as PluginImpl
import xyz.junerver.compose.hooks.userequest.PluginLifecycle as PluginLifecycleImpl
import xyz.junerver.compose.hooks.userequest.PluginOnBefore as PluginOnBeforeImpl
import xyz.junerver.compose.hooks.userequest.PluginOnCancel as PluginOnCancelImpl
import xyz.junerver.compose.hooks.userequest.PluginOnError as PluginOnErrorImpl
import xyz.junerver.compose.hooks.userequest.PluginOnFinally as PluginOnFinallyImpl
import xyz.junerver.compose.hooks.userequest.PluginOnMutate as PluginOnMutateImpl
import xyz.junerver.compose.hooks.userequest.PluginOnRequest as PluginOnRequestImpl
import xyz.junerver.compose.hooks.userequest.PluginOnSuccess as PluginOnSuccessImpl
import xyz.junerver.compose.hooks.userequest.RefreshFn as RefreshFnImpl
import xyz.junerver.compose.hooks.userequest.ReqFn as ReqFnImpl
import xyz.junerver.compose.hooks.userequest.RequestHolder as RequestHolderImpl
import xyz.junerver.compose.hooks.userequest.TableRequestHolder as TableRequestHolderImpl
import xyz.junerver.compose.hooks.userequest.TableRequestParams as TableRequestParamsImpl
import xyz.junerver.compose.hooks.userequest.TableResult as TableResultImpl
import xyz.junerver.compose.hooks.userequest.UseRequestOptions as UseRequestOptionsImpl
import xyz.junerver.compose.hooks.userequest.UseTableRequestOptions as UseTableRequestOptionsImpl
import xyz.junerver.compose.hooks.userequest.noneParams
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.userequest.useTableRequest
import xyz.junerver.compose.hooks.userequest.utils.CachedData as CachedDataImpl
import xyz.junerver.compose.hooks.userequest.utils.clearCache
import xyz.junerver.compose.hooks.usses.OnEventFn as OnEventFnImpl
import xyz.junerver.compose.hooks.usses.SendFn as SendFnImpl
import xyz.junerver.compose.hooks.usses.SseHolder as SseHolderImpl
import xyz.junerver.compose.hooks.usses.SseStreamFn as SseStreamFnImpl
import xyz.junerver.compose.hooks.usses.UseSseOptions as UseSseOptionsImpl
import xyz.junerver.compose.hooks.usses.useSse
import xyz.junerver.compose.hooks.usetable.PaginationScope as PaginationScopeImpl
import xyz.junerver.compose.hooks.usetable.Table as TableImpl
import xyz.junerver.compose.hooks.usetable.TableHolder as TableHolderImpl
import xyz.junerver.compose.hooks.usetable.TableOptions as TableOptionsImpl
import xyz.junerver.compose.hooks.usetable.TableScope as TableScopeImpl
import xyz.junerver.compose.hooks.usetable.core.ColumnDef as ColumnDefImpl
import xyz.junerver.compose.hooks.usetable.core.Row as RowImpl
import xyz.junerver.compose.hooks.usetable.core.RowModel as RowModelImpl
import xyz.junerver.compose.hooks.usetable.core.TableInstance as TableInstanceImpl
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.state.PaginationState as PaginationStateImpl
import xyz.junerver.compose.hooks.usetable.state.SortDescriptor as SortDescriptorImpl
import xyz.junerver.compose.hooks.usetable.state.SortingState as SortingStateImpl
import xyz.junerver.compose.hooks.usetable.state.TableState as TableStateImpl
import xyz.junerver.compose.hooks.usetable.useTable
import xyz.junerver.compose.hooks.utils.currentInstant

/**
 * Public API facade for ComposeHooks.
 *
 * This file re-exports the library's public surface so consumers can import
 * everything from the root package `xyz.junerver.compose.hooks` instead of
 * digging into subpackages. It mirrors the "barrel/facade" pattern used by
 * the Palette design system.
 *
 * Re-exports come in three forms (same approach as Palette):
 *  - `typealias` for public **types** declared in subpackages (Holders,
 *    Options, interfaces, state classes, function types). Each is imported
 *    under an `...Impl` alias and re-aliased to its public name, so the
 *    declaration is not self-referential.
 *  - `@Composable fun rememberXxx(...)` wrappers for the **Compose-style
 *    aliases** of every `useXxx` hook. These are kept as full function
 *    declarations (rather than `val x = ::useX` references) because Kotlin
 *    function references drop default arguments, and most hooks rely on
 *    default parameters.
 *  - `inline fun <reified ...>` wrappers for hooks that capture reified type
 *    parameters (`useDispatch`, `useSelector`, `useEvent*`); these cannot be
 *    expressed as function references at all.
 *
 * Note: types already declared in the root package (e.g. [ReducerHolder],
 * [BooleanHolder], [Reducer], [Dispatch], [UseDebounceOptions], ...) are
 * intentionally NOT re-aliased here.
 */

//region 类型集中导出 — useRequest
typealias RequestHolder<TParams, TData> = RequestHolderImpl<TParams, TData>
typealias TableRequestHolder<T> = TableRequestHolderImpl<T>
typealias TableRequestParams = TableRequestParamsImpl
typealias TableResult<T> = TableResultImpl<T>
typealias UseRequestOptions<TParams, TData> = UseRequestOptionsImpl<TParams, TData>
typealias UseTableRequestOptions<TData> = UseTableRequestOptionsImpl<TData>
typealias FetchState<TParams, TData> = FetchStateImpl<TParams, TData>
typealias OnBeforeReturn<TParams, TData> = OnBeforeReturnImpl<TParams, TData>
typealias OnRequestReturn<TData> = OnRequestReturnImpl<TData>
typealias PluginOnBefore<TParams, TData> = PluginOnBeforeImpl<TParams, TData>
typealias PluginOnRequest<TParams, TData> = PluginOnRequestImpl<TParams, TData>
typealias PluginOnSuccess<TParams, TData> = PluginOnSuccessImpl<TParams, TData>
typealias PluginOnError<TParams> = PluginOnErrorImpl<TParams>
typealias PluginOnFinally<TParams, TData> = PluginOnFinallyImpl<TParams, TData>
typealias PluginOnCancel = PluginOnCancelImpl
typealias PluginOnMutate<TData> = PluginOnMutateImpl<TData>
typealias GenPluginLifecycleFn<TParams, TData> = GenPluginLifecycleFnImpl<TParams, TData>
typealias ComposablePluginGenFn<TParams, TData> = ComposablePluginGenFnImpl<TParams, TData>
typealias ReqFn<TParams> = ReqFnImpl<TParams>
typealias MutateFn<TData> = MutateFnImpl<TData>
typealias RefreshFn = RefreshFnImpl
typealias CancelFn = CancelFnImpl
typealias Plugin<TParams, TData> = PluginImpl<TParams, TData>
typealias PluginLifecycle<TParams, TData> = PluginLifecycleImpl<TParams, TData>
typealias CachedData<TData> = CachedDataImpl<TData>
//endregion

//region 类型集中导出 — useSse (usses)
typealias SseHolder<TParams, TEvent> = SseHolderImpl<TParams, TEvent>
typealias UseSseOptions<TParams, TEvent> = UseSseOptionsImpl<TParams, TEvent>
typealias SseStreamFn<TParams, TEvent> = SseStreamFnImpl<TParams, TEvent>
typealias SendFn<TParams> = SendFnImpl<TParams>
typealias OnEventFn<TEvent> = OnEventFnImpl<TEvent>
//endregion

//region 类型集中导出 — useTable
typealias Table = TableImpl
typealias TableHolder<T> = TableHolderImpl<T>
typealias TableInstance<T> = TableInstanceImpl<T>
typealias TableOptions<T> = TableOptionsImpl<T>
typealias TableScope<T> = TableScopeImpl<T>
typealias PaginationScope = PaginationScopeImpl
typealias ColumnDef<T, V> = ColumnDefImpl<T, V>
typealias Row<T> = RowImpl<T>
typealias RowModel<T> = RowModelImpl<T>
typealias TableState<T> = TableStateImpl<T>
typealias PaginationState = PaginationStateImpl
typealias SortDescriptor = SortDescriptorImpl
typealias SortingState = SortingStateImpl
//endregion

//region 类型集中导出 — useForm
typealias Form = FormImpl
typealias FormInstance = FormInstanceImpl
typealias FormItemState<T> = FormItemStateImpl<T>
typealias ValidationTrigger = ValidationTriggerImpl
typealias Validator = ValidatorImpl
//endregion

//region 类型集中导出 — useRedux
typealias Store = StoreImpl
typealias StoreRecord = StoreRecordImpl
typealias StoreScope = StoreScopeImpl
//endregion

//region 类型集中导出 — 第二批 hook 的 Holder 类型
typealias GetStateHolder<T> = GetStateHolderImpl<T>
typealias ImmutableListHolder<T> = ImmutableListHolderImpl<T>
typealias KeyboardHolder = KeyboardHolderImpl
typealias CopyPasteHolder = CopyPasteHolderImpl
//endregion

/**
 * Compose-style aliases for every hook.
 *
 * Prefer these `rememberXxx` names when writing idiomatic Compose; the
 * underlying `useXxx` functions remain available for React-style usage.
 */

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
fun <TParams, TData : Any> rememberEmptyPlugin(): Plugin<TParams, TData> = useEmptyPlugin()

@Composable
fun <T> rememberTableRequest(
    requestFn: suspend (params: TableRequestParams) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {},
): TableRequestHolder<T> = useTableRequest(requestFn, optionsOf)

@Composable
fun <T> rememberTableRequest(
    requestFn: suspend (page: Int, pageSize: Int) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {},
): TableRequestHolder<T> = useTableRequest(requestFn, optionsOf)

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

//region useEffect
@Composable
fun useEffect(vararg deps: Any?, block: SuspendAsyncFn) = useEffectImpl(*deps, block = block)

@Composable
fun rememberEffect(vararg deps: Any?, block: SuspendAsyncFn) = useEffectImpl(*deps, block = block)
//endregion

@Composable
fun rememberFrontToBackEffect(vararg keys: Any?, effect: () -> Unit) = useFrontToBackEffect(*keys, effect = effect)

//region useBoolean
@Composable
fun useBoolean(default: Boolean = false): BooleanHolder = useBooleanImpl(default)

typealias BooleanHolder = BooleanHolderImpl

@Composable
fun rememberBoolean(default: Boolean = false): BooleanHolder = useBooleanImpl(default)
//endregion

//region useClipboard
@Composable
fun useClipboard(): CopyPasteHolder = useClipboardImpl()

@Composable
fun rememberClipboard(): CopyPasteHolder = useClipboardImpl()
//endregion

@Composable
fun <T> rememberControllable(default: T & Any) = useControllable(default)

@Composable
fun <T> _rememberControllable(default: T) = _useControllable(default)

//region useContext
@Composable
fun <T> useContext(context: ReactContext<T>) = useContextImpl(context)

@Composable
fun <T> rememberContext(context: ReactContext<T>) = useContextImpl(context)
//endregion

@Composable
fun rememberCountdown(optionsOf: UseCountdownOptions.() -> Unit) = useCountdown(optionsOf)

@Composable
fun rememberCounter(initialValue: Int = 0, optionsOf: UseCounterOptions.() -> Unit) = useCounter(initialValue, optionsOf)

//region useCreation
@Composable
fun <T> useCreation(vararg keys: Any?, factory: () -> T) = useCreationImpl(*keys, factory = factory)

@Composable
fun <T> rememberCreation(vararg keys: Any?, factory: () -> T) = useCreationImpl(*keys, factory = factory)
//endregion

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
inline fun <reified T : Any> useEventSubscribe(noinline subscriber: (T) -> Unit) = useEventSubscribeImpl(subscriber)

@Composable
inline fun <reified T : Any> useEventPublish(): (T) -> Unit = useEventPublishImpl()

@Composable
inline fun <reified T : Any> rememberEventSubscribe(noinline subscriber: (T) -> Unit) = useEventSubscribeImpl(subscriber)

@Composable
inline fun <reified T : Any> rememberEventPublish(): (T) -> Unit = useEventPublishImpl()
//endregion

//region useKeyboard
@Composable
fun useKeyboard(): KeyboardHolder = useKeyboardImpl()

@Composable
fun rememberKeyboard(): KeyboardHolder = useKeyboardImpl()
//endregion

//region useGetState
@Composable
fun <T> useGetState(default: T & Any): GetStateHolder<T & Any> = useGetStateImpl(default = default)

@Composable
fun <T> _useGetState(default: T): GetStateHolder<T> = _useGetStateImpl(default = default)

@Composable
fun <T> rememberGetState(default: T & Any) = useGetStateImpl(default = default)

@Composable
fun <T> _rememberGetState(default: T) = _useGetStateImpl(default = default)
//endregion

//region useLatest
@Composable
fun <T> useLatestRef(value: T) = useLatestRefImpl(value)

@Composable
fun <T> useLatestState(value: T) = useLatestStateImpl(value)

@Composable
fun <T> rememberLatestRef(value: T) = useLatestRefImpl(value)

@Composable
fun <T> rememberLatestState(value: T) = useLatestStateImpl(value)
//endregion

//region useList
@Composable
fun <T> useList(elements: Collection<T>) = useListImpl(elements)

@Composable
fun <T> useList(vararg elements: T) = useListImpl(*elements)

@Composable
fun <S, T : S> useListReduce(list: List<T>, operation: (acc: S, T) -> S): State<S?> = useListReduceImpl(list, operation)

@Composable
fun <T> rememberList(elements: Collection<T>) = useListImpl(elements)

@Composable
fun <T> rememberList(vararg elements: T) = useListImpl(*elements)

@Composable
fun <S, T : S> rememberListReduce(list: List<T>, operation: (acc: S, T) -> S): State<S?> = useListReduceImpl(list, operation)
//endregion

//region useMap
@Composable
fun <K, V> useMap(vararg pairs: Pair<K, V>) = useMapImpl(*pairs)

@Composable
fun <K, V> useMap(pairs: Iterable<Pair<K, V>>) = useMapImpl(pairs)

@Composable
fun <K, V> rememberMap(vararg pairs: Pair<K, V>) = useMapImpl(*pairs)

@Composable
fun <K, V> rememberMap(pairs: Iterable<Pair<K, V>>) = useMapImpl(pairs)
//endregion

//region useForm
@Composable
fun Form.rememberForm(): FormInstance = useForm()

@Composable
fun <T> Form.rememberWatch(fieldName: String, formInstance: FormInstance): State<T?> =
    useWatch(fieldName, formInstance)

@Composable
fun Form.rememberFormInstance(): FormInstance = useFormInstance()
//endregion

//region useMount
@Composable
fun useMount(block: SuspendAsyncFn) = useMountImpl(block)

@Composable
fun rememberMount(block: SuspendAsyncFn) = useMountImpl(block)
//endregion

//region useMemoizedFn
@Composable
fun <T, R> useMemoizedFn(fn: suspend DeepRecursiveScope<T, R>.(T) -> R): DeepRecursiveFunction<T, R> = useMemoizedFnImpl(fn)

@Composable
fun <T, R> rememberMemoizedFn(fn: suspend DeepRecursiveScope<T, R>.(T) -> R): DeepRecursiveFunction<T, R> = useMemoizedFnImpl(fn)
//endregion

@Composable
fun rememberNow(optionsOf: UseNowOptions.() -> Unit = {}) = useNow(optionsOf)

//region useNumber
@Composable
fun useDouble(default: Double = 0.0) = useDoubleImpl(default)

@Composable
fun useFloat(default: Float = 0f) = useFloatImpl(default)

@Composable
fun useInt(default: Int = 0) = useIntImpl(default)

@Composable
fun useLong(default: Long = 0L) = useLongImpl(default)

@Composable
fun rememberDouble(default: Double = 0.0) = useDoubleImpl(default)

@Composable
fun rememberFloat(default: Float = 0f) = useFloatImpl(default)

@Composable
fun rememberInt(default: Int = 0) = useIntImpl(default)

@Composable
fun rememberLong(default: Long = 0L) = useLongImpl(default)
//endregion

@Composable
fun <T> rememberPersistent(key: String, defaultValue: T, forceUseMemory: Boolean = false) = usePersistent(key, defaultValue, forceUseMemory)

//region usePrevious
@Composable
fun <T> usePrevious(present: T) = usePreviousImpl(present)

@Composable
fun <T> rememberPrevious(present: T) = usePreviousImpl(present)
//endregion

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

//region useUnmount
@Composable
fun useUnmount(block: () -> Unit) = useUnmountImpl(block)

@Composable
fun rememberUnmount(block: () -> Unit) = useUnmountImpl(block)
//endregion

//region useUnmountedRef
@Composable
fun useUnmountedRef() = useUnmountedRefImpl()

@Composable
fun rememberUnmountedRef() = useUnmountedRefImpl()
//endregion

//region useUpdate
@Composable
fun useUpdate(): () -> Unit = useUpdateImpl()

@Composable
fun rememberUpdate(): () -> Unit = useUpdateImpl()
//endregion

//region useUpdateEffect
@Composable
fun useUpdateEffect(vararg keys: Any?, block: SuspendAsyncFn) = useUpdateEffectImpl(*keys, block = block)

@Composable
fun rememberUpdateEffect(vararg keys: Any?, block: SuspendAsyncFn) = useUpdateEffectImpl(*keys, block = block)
//endregion

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

//region useImmutableList
@Composable
fun <T> useImmutableList(vararg elements: T): ImmutableListHolder<T> = useImmutableListImpl(*elements)

@Composable
fun <S, T : S> useImmutableListReduce(list: PersistentList<T>, operation: (acc: S, T) -> S): State<S> =
    useImmutableListReduceImpl(list, operation)

@Composable
fun <S, T : S> useImmutableListReduceOrNull(list: PersistentList<T>, operation: (acc: S, T) -> S): State<S?> =
    useImmutableListReduceOrNullImpl(list, operation)

@Composable
fun <S, T> useImmutableListFold(list: PersistentList<T>, initial: S, operation: (acc: S, T) -> S): State<S> =
    useImmutableListFoldImpl(list, initial, operation)

@Composable
fun <T> rememberImmutableList(vararg elements: T): ImmutableListHolder<T> = useImmutableListImpl(*elements)

@Composable
fun <S, T : S> rememberImmutableListReduce(list: PersistentList<T>, operation: (acc: S, T) -> S): State<S> =
    useImmutableListReduceImpl(list, operation)

@Composable
fun <S, T : S> rememberImmutableListReduceOrNull(list: PersistentList<T>, operation: (acc: S, T) -> S): State<S?> =
    useImmutableListReduceOrNullImpl(list, operation)

@Composable
fun <S, T> rememberImmutableListFold(list: PersistentList<T>, initial: S, operation: (acc: S, T) -> S): State<S> =
    useImmutableListFoldImpl(list, initial, operation)
//endregion

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

@Composable
fun <T> Table.rememberTable(): TableInstance<T> = useTable()

@Composable
fun <T> Table.rememberTableInstance(): TableInstance<T> = useTableInstance()
//endregion

//region useMath
@Composable
fun useAbs(number: Int): State<Int> = useAbsImpl(number)

@Composable
fun useAbs(number: Double): State<Double> = useAbsImpl(number)

@Composable
fun useAbs(number: Float): State<Float> = useAbsImpl(number)

@Composable
fun useAbs(number: Long): State<Long> = useAbsImpl(number)

@Composable
fun useCeil(number: Double): State<Double> = useCeilImpl(number)

@Composable
fun useCeil(number: Float): State<Float> = useCeilImpl(number)

@Composable
fun useCeil(number: Int): State<Int> = useCeilImpl(number)

@Composable
fun useCeil(number: Long): State<Long> = useCeilImpl(number)

@Composable
fun useFloor(number: Double): State<Double> = useFloorImpl(number)

@Composable
fun useFloor(number: Float): State<Float> = useFloorImpl(number)

@Composable
fun useFloor(number: Int): State<Int> = useFloorImpl(number)

@Composable
fun useFloor(number: Long): State<Long> = useFloorImpl(number)

@Composable
fun useRound(number: Double): State<Double> = useRoundImpl(number)

@Composable
fun useRound(number: Float): State<Float> = useRoundImpl(number)

@Composable
fun useRound(number: Int): State<Int> = useRoundImpl(number)

@Composable
fun useRound(number: Long): State<Long> = useRoundImpl(number)

@Composable
fun useTrunc(number: Double): State<Double> = useTruncImpl(number)

@Composable
fun useTrunc(number: Float): State<Float> = useTruncImpl(number)

@Composable
fun useTrunc(number: Int): State<Int> = useTruncImpl(number)

@Composable
fun useTrunc(number: Long): State<Long> = useTruncImpl(number)

@Composable
fun useMin(a: Int, b: Int): State<Int> = useMinImpl(a, b)

@Composable
fun useMin(a: Long, b: Long): State<Long> = useMinImpl(a, b)

@Composable
fun useMin(a: Float, b: Float): State<Float> = useMinImpl(a, b)

@Composable
fun useMin(a: Double, b: Double): State<Double> = useMinImpl(a, b)

@Composable
fun useMin(a: Int, b: Long): State<Long> = useMinImpl(a, b)

@Composable
fun useMin(a: Long, b: Int): State<Long> = useMinImpl(a, b)

@Composable
fun useMin(a: Float, b: Double): State<Double> = useMinImpl(a, b)

@Composable
fun useMin(a: Double, b: Float): State<Double> = useMinImpl(a, b)

@Composable
fun useMax(a: Int, b: Int): State<Int> = useMaxImpl(a, b)

@Composable
fun useMax(a: Long, b: Long): State<Long> = useMaxImpl(a, b)

@Composable
fun useMax(a: Float, b: Float): State<Float> = useMaxImpl(a, b)

@Composable
fun useMax(a: Double, b: Double): State<Double> = useMaxImpl(a, b)

@Composable
fun useMax(a: Int, b: Long): State<Long> = useMaxImpl(a, b)

@Composable
fun useMax(a: Long, b: Int): State<Long> = useMaxImpl(a, b)

@Composable
fun useMax(a: Float, b: Double): State<Double> = useMaxImpl(a, b)

@Composable
fun useMax(a: Double, b: Float): State<Double> = useMaxImpl(a, b)

@Composable
fun usePow(base: Double, exponent: Double): State<Double> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Double, exponent: Int): State<Double> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Double, exponent: Float): State<Double> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Float, exponent: Float): State<Float> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Float, exponent: Int): State<Float> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Float, exponent: Double): State<Double> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Int, exponent: Int): State<Double> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Int, exponent: Double): State<Double> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Int, exponent: Float): State<Double> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Long, exponent: Int): State<Double> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Long, exponent: Double): State<Double> = usePowImpl(base, exponent)

@Composable
fun usePow(base: Long, exponent: Float): State<Double> = usePowImpl(base, exponent)

@Composable
fun useSqrt(number: Double): State<Double> = useSqrtImpl(number)

@Composable
fun useSqrt(number: Float): State<Float> = useSqrtImpl(number)

@Composable
fun useSqrt(number: Int): State<Double> = useSqrtImpl(number)

@Composable
fun useSqrt(number: Long): State<Double> = useSqrtImpl(number)

@Composable
fun rememberAbs(number: Int): State<Int> = useAbsImpl(number)

@Composable
fun rememberAbs(number: Double): State<Double> = useAbsImpl(number)

@Composable
fun rememberAbs(number: Float): State<Float> = useAbsImpl(number)

@Composable
fun rememberAbs(number: Long): State<Long> = useAbsImpl(number)

@Composable
fun rememberCeil(number: Double): State<Double> = useCeilImpl(number)

@Composable
fun rememberCeil(number: Float): State<Float> = useCeilImpl(number)

@Composable
fun rememberCeil(number: Int): State<Int> = useCeilImpl(number)

@Composable
fun rememberCeil(number: Long): State<Long> = useCeilImpl(number)

@Composable
fun rememberFloor(number: Double): State<Double> = useFloorImpl(number)

@Composable
fun rememberFloor(number: Float): State<Float> = useFloorImpl(number)

@Composable
fun rememberFloor(number: Int): State<Int> = useFloorImpl(number)

@Composable
fun rememberFloor(number: Long): State<Long> = useFloorImpl(number)

@Composable
fun rememberRound(number: Double): State<Double> = useRoundImpl(number)

@Composable
fun rememberRound(number: Float): State<Float> = useRoundImpl(number)

@Composable
fun rememberRound(number: Int): State<Int> = useRoundImpl(number)

@Composable
fun rememberRound(number: Long): State<Long> = useRoundImpl(number)

@Composable
fun rememberTrunc(number: Double): State<Double> = useTruncImpl(number)

@Composable
fun rememberTrunc(number: Float): State<Float> = useTruncImpl(number)

@Composable
fun rememberTrunc(number: Int): State<Int> = useTruncImpl(number)

@Composable
fun rememberTrunc(number: Long): State<Long> = useTruncImpl(number)

@Composable
fun rememberMin(a: Int, b: Int): State<Int> = useMinImpl(a, b)

@Composable
fun rememberMin(a: Long, b: Long): State<Long> = useMinImpl(a, b)

@Composable
fun rememberMin(a: Float, b: Float): State<Float> = useMinImpl(a, b)

@Composable
fun rememberMin(a: Double, b: Double): State<Double> = useMinImpl(a, b)

@Composable
fun rememberMin(a: Int, b: Long): State<Long> = useMinImpl(a, b)

@Composable
fun rememberMin(a: Long, b: Int): State<Long> = useMinImpl(a, b)

@Composable
fun rememberMin(a: Float, b: Double): State<Double> = useMinImpl(a, b)

@Composable
fun rememberMin(a: Double, b: Float): State<Double> = useMinImpl(a, b)

@Composable
fun rememberMax(a: Int, b: Int): State<Int> = useMaxImpl(a, b)

@Composable
fun rememberMax(a: Long, b: Long): State<Long> = useMaxImpl(a, b)

@Composable
fun rememberMax(a: Float, b: Float): State<Float> = useMaxImpl(a, b)

@Composable
fun rememberMax(a: Double, b: Double): State<Double> = useMaxImpl(a, b)

@Composable
fun rememberMax(a: Int, b: Long): State<Long> = useMaxImpl(a, b)

@Composable
fun rememberMax(a: Long, b: Int): State<Long> = useMaxImpl(a, b)

@Composable
fun rememberMax(a: Float, b: Double): State<Double> = useMaxImpl(a, b)

@Composable
fun rememberMax(a: Double, b: Float): State<Double> = useMaxImpl(a, b)

@Composable
fun rememberPow(base: Double, exponent: Double): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Double, exponent: Int): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Double, exponent: Float): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Float, exponent: Float): State<Float> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Float, exponent: Int): State<Float> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Float, exponent: Double): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Int, exponent: Int): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Int, exponent: Double): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Int, exponent: Float): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Long, exponent: Int): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Long, exponent: Double): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberPow(base: Long, exponent: Float): State<Double> = usePowImpl(base, exponent)

@Composable
fun rememberSqrt(number: Double): State<Double> = useSqrtImpl(number)

@Composable
fun rememberSqrt(number: Float): State<Float> = useSqrtImpl(number)

@Composable
fun rememberSqrt(number: Int): State<Double> = useSqrtImpl(number)

@Composable
fun rememberSqrt(number: Long): State<Double> = useSqrtImpl(number)
//endregion
