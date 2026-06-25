@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks
import xyz.junerver.compose.hooks.useresetstate.useResetStateImpl
import xyz.junerver.compose.hooks.useresetstate.ResetStateHolder as ResetStateHolderImpl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.MutableState
import arrow.core.Either
import androidx.compose.runtime.State
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.collections.immutable.PersistentList
import kotlinx.datetime.LocalDateTime
import xyz.junerver.compose.hooks.annotation.ExperimentalComputed
import xyz.junerver.compose.hooks.ComposeComponent
import xyz.junerver.compose.hooks.ToggleFn
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
import xyz.junerver.compose.hooks.useasync.CancelableAsyncHolder as CancelableAsyncHolderImpl
import xyz.junerver.compose.hooks.useasync.useAsyncImpl
import xyz.junerver.compose.hooks.useasync.useCancelableAsyncImpl
import xyz.junerver.compose.hooks.useautoreset.useAutoResetImpl
import xyz.junerver.compose.hooks.usebackfront.useBackToFrontEffectImpl
import xyz.junerver.compose.hooks.usebackfront.useFrontToBackEffectImpl
import xyz.junerver.compose.hooks.usecontrollable.ControllableHolder as ControllableHolderImpl
import xyz.junerver.compose.hooks.usecontrollable._useControllableImpl
import xyz.junerver.compose.hooks.usecontrollable.useControllableImpl
import xyz.junerver.compose.hooks.usecounter.CounterHolder as CounterHolderImpl
import xyz.junerver.compose.hooks.usecounter.UseCounterOptions as UseCounterOptionsImpl
import xyz.junerver.compose.hooks.usecounter.useCounterImpl
import xyz.junerver.compose.hooks.usecyclelist.CycleListHolder as CycleListHolderImpl
import xyz.junerver.compose.hooks.usecyclelist.UseCycleListOptions as UseCycleListOptionsImpl
import xyz.junerver.compose.hooks.usecyclelist.useCycleListImpl
import xyz.junerver.compose.hooks.uselastchanged.useLastChangedImpl
import xyz.junerver.compose.hooks.usepausableeffect.PausableEffectHolder as PausableEffectHolderImpl
import xyz.junerver.compose.hooks.usepausableeffect.usePausableEffectImpl
import xyz.junerver.compose.hooks.usepersistent.PersistentHolder as PersistentHolderImpl
import xyz.junerver.compose.hooks.usepersistent.PersistentContext as PersistentContextImpl
import xyz.junerver.compose.hooks.usepersistent.notifyDefaultPersistentObserver as notifyDefaultPersistentObserverImpl
import xyz.junerver.compose.hooks.usepersistent.usePersistentImpl
import xyz.junerver.compose.hooks.usereducer.ReducerHolder as ReducerHolderImpl
import xyz.junerver.compose.hooks.usereducer.useReducerImpl
import xyz.junerver.compose.hooks.useselectable.IsSelected as IsSelectedImpl
import xyz.junerver.compose.hooks.useselectable.SelectAction as SelectActionImpl
import xyz.junerver.compose.hooks.useselectable.SelectableHolder as SelectableHolderImpl
import xyz.junerver.compose.hooks.useselectable.ToggleSelected as ToggleSelectedImpl
import xyz.junerver.compose.hooks.useselectable.useSelectableImpl
import xyz.junerver.compose.hooks.usesorted.SortedCompareFn as SortedCompareFnImpl
import xyz.junerver.compose.hooks.usesorted.SortedFn as SortedFnImpl
import xyz.junerver.compose.hooks.usesorted.UseSortedOptions as UseSortedOptionsImpl
import xyz.junerver.compose.hooks.usesorted.useSortedImpl
import xyz.junerver.compose.hooks.usestatemachine.MachineGraph as MachineGraphImpl
import xyz.junerver.compose.hooks.usestatemachine.StateMachineHolder as StateMachineHolderImpl
import xyz.junerver.compose.hooks.usestatemachine.StateMachineGraphScope as StateMachineGraphScopeImpl
import xyz.junerver.compose.hooks.usestatemachine.createMachineImpl
import xyz.junerver.compose.hooks.usecontext.ReactContext as ReactContextImpl
import xyz.junerver.compose.hooks.usecontext.createContextImpl
import xyz.junerver.compose.hooks.usecontext.useContextImpl
import xyz.junerver.compose.hooks.useref.MutableRef as MutableRefImpl
import xyz.junerver.compose.hooks.useref.Ref as RefImpl
import xyz.junerver.compose.hooks.useref.getValue as refGetValue
import xyz.junerver.compose.hooks.useref.setValue as refSetValue
import xyz.junerver.compose.hooks.useref.useRefImpl
import xyz.junerver.compose.hooks.usestate.UseStateAsyncOptions as UseStateAsyncOptionsImpl
import xyz.junerver.compose.hooks.usestate._useStateImpl
import xyz.junerver.compose.hooks.usestate.useStateAsyncImpl
import xyz.junerver.compose.hooks.usestate.useStateImpl
import xyz.junerver.compose.hooks.usetoggle.useToggleEitherImpl
import xyz.junerver.compose.hooks.usetoggle.useToggleImpl
import xyz.junerver.compose.hooks.usetoggle.useToggleVisibleImpl
import xyz.junerver.compose.hooks.usestatemachine.useStateMachineImpl
import xyz.junerver.compose.hooks.useundo.UndoHolder as UndoHolderImpl
import xyz.junerver.compose.hooks.useundo.useUndoImpl
import xyz.junerver.compose.hooks.usecountdown.CountdownHolder as CountdownHolderImpl
import xyz.junerver.compose.hooks.usecountdown.FormattedRes as FormattedResImpl
import xyz.junerver.compose.hooks.usecountdown.UseCountdownOptions as UseCountdownOptionsImpl
import xyz.junerver.compose.hooks.usecountdown.useCountdownImpl
import xyz.junerver.compose.hooks.usedateformat.CustomMeridiemFn as CustomMeridiemFnImpl
import xyz.junerver.compose.hooks.usedateformat.DateFormatMessages as DateFormatMessagesImpl
import xyz.junerver.compose.hooks.usedateformat.DefaultChineseDateFormatMessages
import xyz.junerver.compose.hooks.usedateformat.DefaultEnglishDateFormatMessages
import xyz.junerver.compose.hooks.usedateformat.UseDateFormatOptions as UseDateFormatOptionsImpl
import xyz.junerver.compose.hooks.usedateformat.useDateFormatImpl
import xyz.junerver.compose.hooks.usedebounce.UseDebounceOptions as UseDebounceOptionsImpl
import xyz.junerver.compose.hooks.usedebounce.useDebounceEffectImpl
import xyz.junerver.compose.hooks.usedebounce.useDebounceFnImpl
import xyz.junerver.compose.hooks.usedebounce.useDebounceImpl
import xyz.junerver.compose.hooks.useinterval.IntervalHolder as IntervalHolderImpl
import xyz.junerver.compose.hooks.useinterval.UseIntervalOptions as UseIntervalOptionsImpl
import xyz.junerver.compose.hooks.useinterval.useIntervalImpl
import xyz.junerver.compose.hooks.usenow.UseNowOptions as UseNowOptionsImpl
import xyz.junerver.compose.hooks.usenow.useNowImpl
import xyz.junerver.compose.hooks.usethrottle.UseThrottleOptions as UseThrottleOptionsImpl
import xyz.junerver.compose.hooks.usethrottle.useThrottleEffectImpl
import xyz.junerver.compose.hooks.usethrottle.useThrottleFnImpl
import xyz.junerver.compose.hooks.usethrottle.useThrottleImpl
import xyz.junerver.compose.hooks.usetimeago.DefaultChineseTimeAgoMessages as DefaultChineseTimeAgoMessagesImpl
import xyz.junerver.compose.hooks.usetimeago.DefaultEnglishTimeAgoMessages as DefaultEnglishTimeAgoMessagesImpl
import xyz.junerver.compose.hooks.usetimeago.FormatTimeAgoOptions as FormatTimeAgoOptionsImpl
import xyz.junerver.compose.hooks.usetimeago.TimeAgoMessages as TimeAgoMessagesImpl
import xyz.junerver.compose.hooks.usetimeago.TimeAgoMessageFormatter as TimeAgoMessageFormatterImpl
import xyz.junerver.compose.hooks.usetimeago.TimeUnitMessageFormatter as TimeUnitMessageFormatterImpl
import xyz.junerver.compose.hooks.usetimeago.UseTimeAgoOptions as UseTimeAgoOptionsImpl
import xyz.junerver.compose.hooks.usetimeago.formatTimeAgo
import xyz.junerver.compose.hooks.usetimeago.useTimeAgoImpl
import xyz.junerver.compose.hooks.usetimeout.useTimeoutImpl
import xyz.junerver.compose.hooks.usetimeoutfn.StartFn as StartFnImpl
import xyz.junerver.compose.hooks.usetimeoutfn.StopFn as StopFnImpl
import xyz.junerver.compose.hooks.usetimeoutfn.TimeoutFnHolder as TimeoutFnHolderImpl
import xyz.junerver.compose.hooks.usetimeoutfn.UseTimeoutFnOptions as UseTimeoutFnOptionsImpl
import xyz.junerver.compose.hooks.usetimeoutfn.useTimeoutFnImpl
import xyz.junerver.compose.hooks.usetimeoutpoll.TimeoutPollHolder as TimeoutPollHolderImpl
import xyz.junerver.compose.hooks.usetimeoutpoll.UseTimeoutPollOptions as UseTimeoutPollOptionsImpl
import xyz.junerver.compose.hooks.usetimeoutpoll.useTimeoutPollImpl
import xyz.junerver.compose.hooks.usetimestamp.TimestampHolder as TimestampHolderImpl
import xyz.junerver.compose.hooks.usetimestamp.TimestampRefHolder as TimestampRefHolderImpl
import xyz.junerver.compose.hooks.usetimestamp.UseTimestampOptions as UseTimestampOptionsImpl
import xyz.junerver.compose.hooks.usetimestamp.useTimestampImpl
import xyz.junerver.compose.hooks.usetimestamp.useTimestampRefImpl
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
import xyz.junerver.compose.hooks.useredux.useDispatch as useDispatchImpl
import xyz.junerver.compose.hooks.useredux.useDispatchAsync as useDispatchAsyncImpl
import xyz.junerver.compose.hooks.useredux.useSelector as useSelectorImpl
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
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin as useEmptyPluginImpl
import xyz.junerver.compose.hooks.userequest.useRequest as useRequestImpl
import xyz.junerver.compose.hooks.userequest.useTableRequest as useTableRequestImpl
import xyz.junerver.compose.hooks.userequest.utils.CachedData as CachedDataImpl
import xyz.junerver.compose.hooks.userequest.utils.clearCache
import xyz.junerver.compose.hooks.usses.OnEventFn as OnEventFnImpl
import xyz.junerver.compose.hooks.usses.SendFn as SendFnImpl
import xyz.junerver.compose.hooks.usses.SseHolder as SseHolderImpl
import xyz.junerver.compose.hooks.usses.SseStreamFn as SseStreamFnImpl
import xyz.junerver.compose.hooks.usses.UseSseOptions as UseSseOptionsImpl
import xyz.junerver.compose.hooks.usses.useSse as useSseImpl
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
import xyz.junerver.compose.hooks.usetable.useTable as useTableImpl
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

//region 类型集中导出 — 计时/防抖类 hook 的 Options 与 Holder
typealias UseIntervalOptions = UseIntervalOptionsImpl
typealias IntervalHolder = IntervalHolderImpl
typealias UseTimeoutFnOptions = UseTimeoutFnOptionsImpl
typealias TimeoutFnHolder = TimeoutFnHolderImpl
typealias StartFn = StartFnImpl
typealias StopFn = StopFnImpl
typealias UseTimeoutPollOptions = UseTimeoutPollOptionsImpl
typealias TimeoutPollHolder = TimeoutPollHolderImpl
typealias UseDebounceOptions = UseDebounceOptionsImpl
typealias UseThrottleOptions = UseThrottleOptionsImpl
typealias UseCountdownOptions = UseCountdownOptionsImpl
typealias CountdownHolder = CountdownHolderImpl
typealias FormattedRes = FormattedResImpl
typealias UseTimestampOptions = UseTimestampOptionsImpl
typealias TimestampHolder = TimestampHolderImpl
typealias TimestampRefHolder = TimestampRefHolderImpl
typealias UseNowOptions = UseNowOptionsImpl
typealias UseDateFormatOptions = UseDateFormatOptionsImpl
typealias CustomMeridiemFn = CustomMeridiemFnImpl
typealias DateFormatMessages = DateFormatMessagesImpl
typealias FormatTimeAgoOptions = FormatTimeAgoOptionsImpl
typealias UseTimeAgoOptions = UseTimeAgoOptionsImpl
typealias TimeAgoMessages = TimeAgoMessagesImpl
typealias TimeAgoMessageFormatter = TimeAgoMessageFormatterImpl
typealias TimeUnitMessageFormatter = TimeUnitMessageFormatterImpl

val DefaultChineseTimeAgoMessages: TimeAgoMessages = DefaultChineseTimeAgoMessagesImpl
val DefaultEnglishTimeAgoMessages: TimeAgoMessages = DefaultEnglishTimeAgoMessagesImpl
//endregion

//region 类型集中导出 — 状态/其余 hook 的 Options 与 Holder
typealias CancelableAsyncHolder = CancelableAsyncHolderImpl
typealias ControllableHolder<T> = ControllableHolderImpl<T>
typealias UseCounterOptions = UseCounterOptionsImpl
typealias CounterHolder = CounterHolderImpl
typealias UseCycleListOptions<T> = UseCycleListOptionsImpl<T>
typealias CycleListHolder<T> = CycleListHolderImpl<T>
typealias PausableEffectHolder = PausableEffectHolderImpl
typealias PersistentHolder<T> = PersistentHolderImpl<T>

val PersistentContext = PersistentContextImpl
typealias ReducerHolder<S, A> = ReducerHolderImpl<S, A>
typealias IsSelected<KEY> = IsSelectedImpl<KEY>
typealias ToggleSelected<KEY> = ToggleSelectedImpl<KEY>
typealias SelectAction = SelectActionImpl
typealias SelectableHolder<KEY, ITEM> = SelectableHolderImpl<KEY, ITEM>
typealias SortedCompareFn<T> = SortedCompareFnImpl<T>
typealias SortedFn<T> = SortedFnImpl<T>
typealias UseSortedOptions<T> = UseSortedOptionsImpl<T>
typealias MachineGraph<S, E, CTX> = MachineGraphImpl<S, E, CTX>
typealias StateMachineHolder<S, E, CTX> = StateMachineHolderImpl<S, E, CTX>
typealias StateMachineGraphScope<S, E, CTX> = StateMachineGraphScopeImpl<S, E, CTX>
typealias UndoHolder<T> = UndoHolderImpl<T>
typealias ResetStateHolder<T> = ResetStateHolderImpl<T>
typealias Ref<T> = RefImpl<T>
typealias MutableRef<T> = MutableRefImpl<T>
typealias ReactContext<T> = ReactContextImpl<T>
typealias UseStateAsyncOptions = UseStateAsyncOptionsImpl
//endregion

/**
 * Compose-style aliases for every hook.
 *
 * Prefer these `rememberXxx` names when writing idiomatic Compose; the
 * underlying `useXxx` functions remain available for React-style usage.
 */

//region useRequest/useSse standalone 根包重导出
@Composable
fun <TParams, TData : Any> useRequest(
    requestFn: SuspendNormalFunction<TParams?, TData>,
    optionsOf: UseRequestOptions<TParams, TData>.() -> Unit = {},
    plugins: Array<@Composable (UseRequestOptions<TParams, TData>) -> Plugin<TParams, TData>> = emptyArray(),
) = useRequestImpl(requestFn, optionsOf, plugins)

@Composable
fun <T> useTableRequest(
    requestFn: suspend (params: TableRequestParams) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {},
): TableRequestHolder<T> = useTableRequestImpl(requestFn, optionsOf)

@Composable
fun <T> useTableRequest(
    requestFn: suspend (page: Int, pageSize: Int) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {},
): TableRequestHolder<T> = useTableRequestImpl(requestFn, optionsOf)

@Composable
fun <TParams, TData : Any> useEmptyPlugin(): Plugin<TParams, TData> = useEmptyPluginImpl()

@Composable
fun <TParams, TEvent> useSse(
    streamFn: SseStreamFn<TParams, TEvent>,
    optionsOf: UseSseOptions<TParams, TEvent>.() -> Unit = {},
): SseHolder<TParams, TEvent> = useSseImpl(streamFn, optionsOf)
//endregion

//region useRedux
@Composable
inline fun <reified A> useDispatch(alias: String? = null): Dispatch<A> = useDispatchImpl(alias)

@Composable
inline fun <reified A> useDispatchAsync(
    alias: String? = null,
    noinline onBefore: DispatchCallback<A>? = null,
    noinline onFinally: DispatchCallback<A>? = null,
): DispatchAsync<A> = useDispatchAsyncImpl(alias, onBefore, onFinally)

@Composable
inline fun <reified T> useSelector(alias: String? = null): State<T> = useSelectorImpl(alias)

@Composable
inline fun <reified T, R> useSelector(alias: String? = null, crossinline block: T.() -> R) = useSelectorImpl(alias, block)

@Composable
inline fun <reified A> rememberDispatch(alias: String? = null): Dispatch<A> = useDispatchImpl(alias)

@Composable
inline fun <reified A> rememberDispatchAsync(
    alias: String? = null,
    noinline onBefore: DispatchCallback<A>? = null,
    noinline onFinally: DispatchCallback<A>? = null,
): DispatchAsync<A> = useDispatchAsyncImpl(alias, onBefore, onFinally)

@Composable
inline fun <reified T> rememberSelector(alias: String? = null): State<T> = useSelectorImpl(alias)

@Composable
inline fun <reified T, R> rememberSelector(alias: String? = null, crossinline block: T.() -> R) = useSelectorImpl(alias, block)
//endregion

@Composable
fun <TParams, TData : Any> rememberRequest(
    requestFn: SuspendNormalFunction<TParams?, TData>,
    optionsOf: UseRequestOptions<TParams, TData>.() -> Unit = {},
    plugins: Array<@Composable (UseRequestOptions<TParams, TData>) -> Plugin<TParams, TData>> = emptyArray(),
) = useRequestImpl(requestFn, optionsOf, plugins)

@Composable
fun <TParams, TData : Any> rememberEmptyPlugin(): Plugin<TParams, TData> = useEmptyPluginImpl()

@Composable
fun <T> rememberTableRequest(
    requestFn: suspend (params: TableRequestParams) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {},
): TableRequestHolder<T> = useTableRequestImpl(requestFn, optionsOf)

@Composable
fun <T> rememberTableRequest(
    requestFn: suspend (page: Int, pageSize: Int) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {},
): TableRequestHolder<T> = useTableRequestImpl(requestFn, optionsOf)

@Composable
fun <TParams, TEvent> rememberSse(
    streamFn: SseStreamFn<TParams, TEvent>,
    optionsOf: UseSseOptions<TParams, TEvent>.() -> Unit = {},
): SseHolder<TParams, TEvent> = useSseImpl(streamFn, optionsOf)

//region useAsync
@Composable
fun rememberAsync(block: SuspendAsyncFn) = useAsyncImpl(block)

@Composable
fun rememberAsync(): AsyncRunFn = useAsyncImpl()

@Composable
fun rememberCancelableAsync(): CancelableAsyncHolder = useCancelableAsyncImpl()
//endregion

@Composable
fun <T> rememberAutoReset(default: T & Any, interval: Duration) = useAutoResetImpl(default, interval)

@Composable
fun rememberBackToFrontEffect(vararg keys: Any?, effect: () -> Unit) = useBackToFrontEffectImpl(*keys, effect = effect)

//region useEffect
@Composable
fun useEffect(vararg deps: Any?, block: SuspendAsyncFn) = useEffectImpl(*deps, block = block)

@Composable
fun rememberEffect(vararg deps: Any?, block: SuspendAsyncFn) = useEffectImpl(*deps, block = block)
//endregion

@Composable
fun rememberFrontToBackEffect(vararg keys: Any?, effect: () -> Unit) = useFrontToBackEffectImpl(*keys, effect = effect)

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
fun <T> rememberControllable(default: T & Any) = useControllableImpl(default)

@Composable
fun <T> _rememberControllable(default: T) = _useControllableImpl(default)

//region useContext
@Composable
fun <T> useContext(context: ReactContext<T>) = useContextImpl(context)

@Composable
fun <T> rememberContext(context: ReactContext<T>) = useContextImpl(context)
//endregion

@Composable
fun rememberCountdown(optionsOf: UseCountdownOptions.() -> Unit) = useCountdownImpl(optionsOf)

@Composable
fun rememberCounter(initialValue: Int = 0, optionsOf: UseCounterOptions.() -> Unit) = useCounterImpl(initialValue, optionsOf)

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
): State<String> = useDateFormatImpl(date, formatStr, optionsOf)

@Composable
fun rememberDateFormat(date: LocalDateTime, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormatImpl(date, formatStr, optionsOf)

@Composable
fun rememberDateFormat(date: String, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormatImpl(date, formatStr, optionsOf)

@Composable
fun rememberDateFormat(date: Long, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormatImpl(date, formatStr, optionsOf)
//endregion

//region useDebounce
@Composable
fun <S> rememberDebounce(value: S, optionsOf: UseDebounceOptions.() -> Unit = {}) = useDebounceImpl(value, optionsOf)

@Composable
fun <TParams> rememberDebounceFn(fn: VoidFunction<TParams>, optionsOf: UseDebounceOptions.() -> Unit = {}) = useDebounceFnImpl(fn, optionsOf)

@Composable
fun rememberDebounceEffect(vararg keys: Any?, optionsOf: UseDebounceOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useDebounceEffectImpl(*keys, optionsOf = optionsOf, block = block)

@Composable
fun LaunchedDebounceEffect(vararg keys: Any?, optionsOf: UseDebounceOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useDebounceEffectImpl(*keys, optionsOf = optionsOf, block = block)
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
fun rememberNow(optionsOf: UseNowOptions.() -> Unit = {}) = useNowImpl(optionsOf)

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
fun <T> rememberPersistent(key: String, defaultValue: T, forceUseMemory: Boolean = false) = usePersistentImpl(key, defaultValue, forceUseMemory)

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
fun <T> rememberResetState(default: T & Any) = useResetStateImpl(default)

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
fun <S> rememberThrottle(value: S, optionsOf: UseThrottleOptions.() -> Unit = {}) = useThrottleImpl(value, optionsOf)

@Composable
fun <TParams> rememberThrottleFn(fn: VoidFunction<TParams>, optionsOf: UseThrottleOptions.() -> Unit = {}) = useThrottleFnImpl(fn, optionsOf)

@Composable
fun rememberThrottleEffect(vararg keys: Any?, optionsOf: UseThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useThrottleEffectImpl(*keys, optionsOf = optionsOf, block = block)

@Composable
fun LaunchedThrottleEffect(vararg keys: Any?, optionsOf: UseThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useThrottleEffectImpl(*keys, optionsOf = optionsOf, block = block)
//endregion

@Deprecated(message = "useTimeout with delay and block is deprecated. Use rememberTimeoutFn instead.")
@Composable
fun rememberTimeout(delay: Duration = 1.seconds, block: () -> Unit) {
    useTimeoutFnImpl(fn = { block() }, interval = delay)
}

@Composable
fun rememberTimestamp(optionsOf: UseTimestampOptions.() -> Unit = {}, autoResume: Boolean = true) = useTimestampImpl(optionsOf, autoResume)

@Composable
fun rememberTimestampRef(optionsOf: UseTimestampOptions.() -> Unit = {}, autoResume: Boolean = true) =
    useTimestampRef(optionsOf, autoResume)

@Composable
fun rememberTimeAgo(time: Instant, optionsOf: UseTimeAgoOptions.() -> Unit = {}): State<String> = useTimeAgoImpl(time, optionsOf)

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
fun <T> rememberUndo(initialPresent: T) = useUndoImpl(initialPresent)

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
fun rememberLastChanged(source: Any?): State<Instant> = useLastChangedImpl(source)

@Composable
fun <KEY, ITEM> rememberSelectable(
    selectionMode: SelectionMode<KEY>,
    items: List<ITEM>,
    keyProvider: (ITEM) -> KEY,
): SelectableHolder<KEY, ITEM> = useSelectableImpl(selectionMode, items, keyProvider)

@Composable
fun <S : Any, E, CTX> rememberStateMachine(machineGraph: Ref<MachineGraph<S, E, CTX>>): StateMachineHolder<S, E, CTX> =
    useStateMachine(machineGraph)

@Composable
fun rememberTimeoutFn(fn: SuspendAsyncFn, interval: Duration = 1.seconds, optionsOf: UseTimeoutFnOptions.() -> Unit = {}): TimeoutFnHolder =
    useTimeoutFnImpl(fn, interval, optionsOf)

@Composable
fun rememberTimeoutPoll(
    fn: SuspendAsyncFn,
    interval: Duration = 1.seconds,
    optionsOf: UseTimeoutPollOptions.() -> Unit = {},
): TimeoutPollHolder = useTimeoutPollImpl(fn, interval, optionsOf)

@Composable
fun rememberTimeoutPoll(fn: SuspendAsyncFn, interval: Duration = 1.seconds, immediate: Boolean = true) =
    useTimeoutPollImpl(fn, interval, immediate)

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
fun <T> rememberSorted(source: List<T>, compareFn: SortedCompareFn<T>): State<List<T>> = useSortedImpl(source, compareFn)

@Composable
fun <T> rememberSorted(source: List<T>, optionsOf: UseSortedOptions<T>.() -> Unit = {}): State<List<T>> = useSortedImpl(source, optionsOf)
//endregion

//region useTable
@Composable
fun <T> useTable(
    data: List<T>,
    columns: List<ColumnDef<T, *>>,
    optionsOf: TableOptions<T>.() -> Unit = {},
): TableHolder<T> = useTableImpl(data, columns, optionsOf)

@Composable
fun <T> rememberTable(
    data: List<T>,
    columns: List<ColumnDef<T, *>>,
    optionsOf: TableOptions<T>.() -> Unit = {},
): TableHolder<T> = useTableImpl(data, columns, optionsOf)

@Deprecated("Use useTable(data, columns) with explicit data and columns instead.")
@Composable
fun <T> Table.rememberTable(): TableInstance<T> = error("Table.useTable() is deprecated. Use useTable(data, columns) instead.")

@Deprecated("Use useTable(data, columns) with explicit data and columns instead.")
@Composable
fun <T> Table.rememberTableInstance(): TableInstance<T> = error("Table.useTable() is deprecated. Use useTable(data, columns) instead.")
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

//region 计时/防抖类 hook 的 useXxx 根包重导出
@Composable
fun useInterval(optionsOf: UseIntervalOptions.() -> Unit = {}, block: SuspendAsyncFn): IntervalHolder = useIntervalImpl(optionsOf, block)

@Composable
fun useInterval(optionsOf: UseIntervalOptions.() -> Unit = {}, ready: Boolean, block: SuspendAsyncFn) =
    useIntervalImpl(optionsOf, ready, block)

@Composable
fun useTimeoutFn(fn: SuspendAsyncFn, interval: Duration = 1.seconds, optionsOf: UseTimeoutFnOptions.() -> Unit = {}): TimeoutFnHolder =
    useTimeoutFnImpl(fn, interval, optionsOf)

@Composable
fun useTimeoutPoll(
    fn: SuspendAsyncFn,
    interval: Duration = 1.seconds,
    optionsOf: UseTimeoutPollOptions.() -> Unit,
): TimeoutPollHolder = useTimeoutPollImpl(fn, interval, optionsOf)

@Composable
fun useTimeoutPoll(fn: SuspendAsyncFn, interval: Duration = 1.seconds, immediate: Boolean = true) =
    useTimeoutPollImpl(fn, interval, immediate)

@Composable
fun <S> useDebounce(value: S, optionsOf: UseDebounceOptions.() -> Unit = {}): State<S> = useDebounceImpl(value, optionsOf)

@Composable
fun <TParams> useDebounceFn(fn: VoidFunction<TParams>, optionsOf: UseDebounceOptions.() -> Unit = {}): VoidFunction<TParams> =
    useDebounceFnImpl(fn, optionsOf)

@Composable
fun useDebounceEffect(vararg keys: Any?, optionsOf: UseDebounceOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useDebounceEffectImpl(*keys, optionsOf = optionsOf, block = block)

@Composable
fun <S> useThrottle(value: S, optionsOf: UseThrottleOptions.() -> Unit = {}): State<S> = useThrottleImpl(value, optionsOf)

@Composable
fun <TParams> useThrottleFn(fn: VoidFunction<TParams>, optionsOf: UseThrottleOptions.() -> Unit = {}): VoidFunction<TParams> =
    useThrottleFnImpl(fn, optionsOf)

@Composable
fun useThrottleEffect(vararg keys: Any?, optionsOf: UseThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) =
    useThrottleEffectImpl(*keys, optionsOf = optionsOf, block = block)

@Composable
fun useCountdown(optionsOf: UseCountdownOptions.() -> Unit): CountdownHolder = useCountdownImpl(optionsOf)

@Composable
fun useTimestamp(optionsOf: UseTimestampOptions.() -> Unit = {}, autoResume: Boolean = true): TimestampHolder =
    useTimestampImpl(optionsOf, autoResume)

@Composable
fun useTimestampRef(optionsOf: UseTimestampOptions.() -> Unit = {}, autoResume: Boolean = true): TimestampRefHolder =
    useTimestampRefImpl(optionsOf, autoResume)

@Composable
fun useNow(optionsOf: UseNowOptions.() -> Unit = {}) = useNowImpl(optionsOf)

@Composable
fun useDateFormat(
    date: Instant = currentInstant,
    formatStr: String = "HH:mm:ss",
    optionsOf: UseDateFormatOptions.() -> Unit = {},
): State<String> = useDateFormatImpl(date, formatStr, optionsOf)

@Composable
fun useDateFormat(date: LocalDateTime, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormatImpl(date, formatStr, optionsOf)

@Composable
fun useDateFormat(date: String, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormatImpl(date, formatStr, optionsOf)

@Composable
fun useDateFormat(date: Long, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormatImpl(date, formatStr, optionsOf)

@Composable
fun useTimeAgo(time: Instant, optionsOf: UseTimeAgoOptions.() -> Unit = {}): State<String> = useTimeAgoImpl(time, optionsOf)

@Deprecated(message = "useTimeout with delay and block is deprecated. Use useTimeoutFn instead.")
@Composable
fun useTimeout(delay: Duration = 1.seconds, block: () -> Unit) {
    useTimeoutFnImpl(fn = { block() }, interval = delay)
}
//endregion

//region 状态/其余 hook 的 useXxx 根包重导出
@Composable
fun useAsync(block: SuspendAsyncFn): () -> Unit = useAsyncImpl(block)

@Composable
fun useAsync(): AsyncRunFn = useAsyncImpl()

@Composable
fun useCancelableAsync(): CancelableAsyncHolder = useCancelableAsyncImpl()

@Composable
fun <T> useAutoReset(default: T & Any, interval: Duration): MutableState<T & Any> = useAutoResetImpl(default, interval)

@Composable
fun useBackToFrontEffect(vararg deps: Any?, effect: () -> Unit) = useBackToFrontEffectImpl(*deps, effect = effect)

@Composable
fun useFrontToBackEffect(vararg deps: Any?, effect: () -> Unit) = useFrontToBackEffectImpl(*deps, effect = effect)

@Composable
fun <T> useControllable(default: T & Any): ControllableHolder<T & Any> = useControllableImpl(default)

@Composable
fun <T> _useControllable(default: T): ControllableHolder<T> = _useControllableImpl(default)

@Composable
fun <T> useResetState(default: T & Any): ResetStateHolder<T & Any> = useResetStateImpl(default)

@Composable
fun useCounter(initialValue: Int = 0, optionsOf: UseCounterOptions.() -> Unit): CounterHolder =
    useCounterImpl(initialValue, optionsOf)

@Composable
fun <T> useUndo(initialPresent: T): UndoHolder<T> = useUndoImpl(initialPresent)

@Composable
fun <S, A> useReducer(
    reducer: Reducer<S, A>,
    initialState: S,
    middlewares: Array<Middleware<S, A>> = emptyArray(),
): ReducerHolder<S, A> = useReducerImpl(reducer, initialState, middlewares)

@Composable
fun <T> usePersistent(key: String, defaultValue: T, forceUseMemory: Boolean = false): PersistentHolder<T> =
    usePersistentImpl(key, defaultValue, forceUseMemory)

@Composable
fun usePausableEffect(vararg deps: Any?, block: SuspendAsyncFn): PausableEffectHolder =
    usePausableEffectImpl(*deps, block = block)

@Composable
fun <KEY, ITEM> useSelectable(
    selectionMode: SelectionMode<KEY>,
    items: List<ITEM>,
    keyProvider: (ITEM) -> KEY,
): SelectableHolder<KEY, ITEM> = useSelectableImpl(selectionMode, items, keyProvider)

@Composable
fun <S : Any, E, CTX> useStateMachine(machineGraph: Ref<MachineGraph<S, E, CTX>>): StateMachineHolder<S, E, CTX> =
    useStateMachineImpl(machineGraph)

@Composable
fun <S : Any, E, CTX> createMachine(init: StateMachineGraphScope<S, E, CTX>.() -> Unit): Ref<MachineGraph<S, E, CTX>> =
    createMachineImpl(init)

@Composable
fun <T> useCycleList(list: PersistentList<T>, optionsOf: UseCycleListOptions<T>.() -> Unit = {}): CycleListHolder<T> =
    useCycleListImpl(list, optionsOf)

@Composable
fun useLastChanged(source: Any?): State<Instant> = useLastChangedImpl(source)

@Composable
fun <T> useSorted(source: List<T>, compareFn: SortedCompareFn<T>): State<List<T>> = useSortedImpl(source, compareFn)

@Composable
fun <T> useSorted(source: List<T>, optionsOf: UseSortedOptions<T>.() -> Unit = {}): State<List<T>> =
    useSortedImpl(source, optionsOf)
//endregion

//region 函数型根包重导出
fun notifyDefaultPersistentObserver(key: String) = notifyDefaultPersistentObserverImpl(key)
//endregion

//region 基础 hook 的 useXxx 根包重导出（useState/useRef/useToggle/useContext）
@Composable
fun <T> useState(default: T & Any): MutableState<T & Any> = useStateImpl(default)

@Composable
fun <T> useState(vararg keys: Any?, factory: () -> T): State<T> = useStateImpl(*keys, factory = factory)

@ExperimentalComputed
@Composable
fun <T> useStateAsync(
    vararg keys: Any?,
    initValue: T? = null,
    optionsOf: UseStateAsyncOptions.() -> Unit = {},
    factory: suspend () -> T,
): State<T?> = useStateAsyncImpl(*keys, initValue = initValue, optionsOf = optionsOf, factory = factory)

@Composable
fun <T> _useState(default: T): MutableState<T> = _useStateImpl(default)

@Composable
fun <T> useRef(default: T): MutableRef<T> = useRefImpl(default)

@Composable
fun <T> useToggle(defaultValue: T? = null, reverseValue: T? = null): Pair<T?, ToggleFn> =
    useToggleImpl(defaultValue, reverseValue)

@Composable
fun <L, R> useToggleEither(defaultValue: L? = null, reverseValue: R? = null): Pair<Either<L?, R?>, ToggleFn> =
    useToggleEitherImpl(defaultValue, reverseValue)

@Composable
fun useToggleVisible(isVisible: Boolean = false, content: ComposeComponent): Pair<ComposeComponent, ToggleFn> =
    useToggleVisibleImpl(isVisible, content)

@Composable
fun useToggleVisible(isFirst: Boolean = true, content1: ComposeComponent, content2: ComposeComponent): Pair<ComposeComponent, ToggleFn> =
    useToggleVisibleImpl(isFirst, content1, content2)

/**
 * Create a context object. Implemented directly in the root package so that
 * cross-module consumers calling it in non-@Composable scopes (e.g. `by lazy`)
 * are not flagged by the Compose compiler — the anonymous object with a
 * @Composable [ReactContext.Provider] member must be created in the same
 * compilation unit the consumer sees.
 */
fun <T> createContext(initialValue: T): ReactContext<T> = object : ReactContext<T> {
    override val LocalCtx = compositionLocalOf { initialValue }

    @Composable
    override fun Provider(value: T, content: ComposeComponent) {
        CompositionLocalProvider(
            LocalCtx provides value,
            content = content,
        )
    }
}
//endregion
