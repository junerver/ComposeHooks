@file:Suppress("unused")

package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.CoroutineScope

/*
  Description: Types
  Author: Junerver
  Date: 2024/2/2-8:02
  Email: junerver@gmail.com
  Version: v1.0
*/

// 原始函数模型 (TParams) -> TData
internal typealias TParams = Array<Any?> // 原函数可变长度的参数

// 对所有函数固定抽象
internal typealias NormalFunction<TData> = (TParams) -> TData
typealias SuspendNormalFunction<TData> = suspend (TParams) -> TData
internal typealias VoidFunction = NormalFunction<Unit>
internal typealias SuspendVoidFunction = SuspendNormalFunction<Unit>

// 最常规的函数 ()->Unit
internal typealias NoParamsVoidFunction = () -> Unit

internal typealias ComposeComponent = @Composable () -> Unit

internal typealias PauseFn = () -> Unit
internal typealias ResumeFn = () -> Unit
internal typealias IsActive = State<Boolean>

internal typealias ToggleFn = () -> Unit

internal typealias OnEndCallback = () -> Unit

internal typealias SetValueFn<T> = (T) -> Unit
internal typealias ResetFn = () -> Unit
internal typealias GetValueFn<T> = () -> T

internal typealias SuspendAsyncFn = suspend CoroutineScope.() -> Unit
internal typealias AsyncRunFn = (SuspendAsyncFn) -> Unit

// useCounter
internal typealias IncFn = (Int) -> Unit
internal typealias DecFn = (Int) -> Unit

// useDisableScreenshot
internal typealias DisableFn = () -> Unit
internal typealias EnableFn = () -> Unit
internal typealias IsDisabled = State<Boolean>

// useFlashlight
internal typealias TurnOnFn = () -> Unit
internal typealias TurnOffFn = () -> Unit

// useWakeLock
internal typealias RequestFn = () -> Unit
internal typealias ReleaseFn = () -> Unit

// useBoolean
internal typealias SetTrueFn = () -> Unit
internal typealias SetFalseFn = () -> Unit

// useWindowFlag
internal typealias AddFlagsFn = () -> Unit
internal typealias ClearFlagsFn = () -> Unit
internal typealias IsFlagsAdded = State<Boolean>

// useUndo
internal typealias ResetValueFn<T> = (T) -> Unit
internal typealias RedoFn = () -> Unit
internal typealias UndoFn = () -> Unit
internal typealias CanUndo = State<Boolean>
internal typealias CanRedo = State<Boolean>

// useClipboard
internal typealias CopyFn = (String) -> Unit
internal typealias PasteFn = () -> String

// useKeyboard
internal typealias HideKeyboardFn = () -> Unit
internal typealias ShowKeyboardFn = () -> Unit

// usePersistent
/** pass in the key to get the persistent object */
internal typealias PersistentGet = (String, Any) -> Any

/** Pass in the key, persist the object, and perform persistence */
internal typealias PersistentSave = (String, Any?) -> Unit

/** Perform clear persistent by pass key */
internal typealias PersistentClear = (String) -> Unit
internal typealias HookClear = () -> Unit

/** Perform persistent save */
internal typealias SaveToPersistent<T> = (T?) -> Unit
internal typealias PersistentContextValue = Triple<PersistentGet, PersistentSave, PersistentClear>

// useRedux
typealias Reducer<S, A> = (prevState: S, action: A) -> S
typealias Dispatch<A> = (A) -> Unit
typealias DispatchAsync<A> = (block: suspend CoroutineScope.(Dispatch<A>) -> A) -> Unit
typealias Middleware<S, A> = (dispatch: Dispatch<A>, state: S) -> Dispatch<A>
internal typealias DispatchCallback<A> = (Dispatch<A>) -> Unit

/**
 * 优化函数调用形式，将导出的函数伪装成普通函数的样子，无需对参数进行[arrayOf]，可能需要手动导包：
 * ```
 * import xyz.junerver.compose.hooks.invoke
 * ```
 */
operator fun <TData> NormalFunction<TData>.invoke(vararg params: Any?) = this(arrayOf(*params))

operator fun VoidFunction.invoke(vararg params: Any?) = this(arrayOf(*params))

/**
 * 优化函数调用，通过使用[Either]，可以实现在kotlin平台的解构多态，可以实现如下代码，增加灵活性
 * ```kotlin
 * import xyz.junerver.compose.hooks.invoke
 * // before
 * set(3.left())
 * set({value:Int ->
 *      value/3
 * }.right())
 * // after
 * set(3)
 * set { value: Int -> value / 3 }
 * ```
 *
 * 但是必须要明确Either的泛型类型，不能直接使用泛型
 */
internal typealias SetterEither<T> = Either<T, (T) -> T>

operator fun <T> SetValueFn<SetterEither<T>>.invoke(leftValue: T) = this(leftValue.left())

operator fun <T> SetValueFn<SetterEither<T>>.invoke(rightValue: (T) -> T) = this(rightValue.right())

/**
 * 退化函数调用，将`SetValueFn<SetterEither<T>>`转换为`SetValueFn<T>`，方便使用
 */
fun <T> SetValueFn<SetterEither<T>>.left(): SetValueFn<T> = { leftValue -> this(leftValue.left()) }
