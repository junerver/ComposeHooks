@file:Suppress("unused")

package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlin.coroutines.cancellation.CancellationException

/*
  Description:
  Author: Junerver
  Date: 2024/2/29-9:00
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Description: 在compose中使用state需要写一点模板代码，虽然谈不上有多麻烦，但是还是能简化一下的；
 * 返回值是[MutableState]，可以解构后使用。如果是可空对象需要显示声明：
 * ```
 * val (state,setState) = _useState<Boolean?>(null)
 * val otherState by useState("")
 * ```
 * 这样做还有一个好处就是减少闭包层级，我们可以轻松的构造一些简单的受控组件：
 * ```
 * val (state, setState) = useState("")
 * OutlinedTextField(
 *     value = name,
 *     onValueChange = setState,
 *     label = { Text("Name") }
 * )
 * ```
 * 重要提示：这种方式在便捷的同时也有俩个缺陷：
 * 1. 带来闭包问题，当我们在某个闭包函数中使用状态，会出现闭包问题，需要使用[useLatestRef]避免。
 * 2. 当你快速的（毫秒级）调用set函数时，会触发 Compose 的重组优化，导致状态丢失。
 *
 * 这种情况下，可以退回到使用 `by` 委托来获取状态对象
 *
 * ---
 *
 * Important note: While this method is convenient, it also has two flaws:
 * 1. It causes closure problems. When we use state in a closure function,
 *  closure problems will occur, need to use [useLatestRef] to avoid.
 * 2. When you call the set function quickly(millisecond level),
 *  Compose's recompose optimization will be triggered, resulting in state loss.
 *
 * In this case, you can fall back to using the `by` delegate to get the state object
 *
 */
@Suppress("UNCHECKED_CAST")
@Composable
fun <T> useState(default: T & Any): MutableState<T & Any> = when (default) {
    is Int -> useInt(default)
    is Float -> useFloat(default)
    is Double -> useDouble(default)
    is Long -> useLong(default)
    else -> _useState(default)
} as MutableState<T & Any>

/**
 * 用于方便的创建派生状态，派生状态不同于普通的[MutableState]，他是一个只读的状态，
 * 它只会在依赖发生变化时重新计算改变状态，在其他的框架中也称之为计算属性或者计算状态。
 *
 *  Used to conveniently create derived states. Derived state is different from
 *  ordinary [MutableState]. It is a read-only state. It will only recalculate
 *  the changed state when dependencies change. It is also called a
 *  *Computed Properties* in other frameworks.
 *
 * @param T  Derived object type
 * @param keys  Dependencies, calculations will be reinitiated when they change, changing the derived state
 * @param factory  Function used to generate derived objects
 * @receiver
 */
@Composable
fun <T> useState(vararg keys: Any?, factory: () -> T): State<T> = remember(keys = keys) {
    derivedStateOf(factory)
}

/**
 * Configuration options for the [useStateAsync] hook.
 *
 * @property lazy When set to true, the async computation will only be triggered when the state is accessed,
 *                rather than immediately when the component is composed. Default is false.
 * @property onError A callback function that is invoked when an error occurs during the async computation.
 *                  By default, it prints the stack trace of the error.
 */
@Stable
data class StateAsyncOptions internal constructor(
    var lazy: Boolean = false,
    var onError: (Throwable) -> Unit = { error -> error.printStackTrace() },
) {
    companion object : Options<StateAsyncOptions>(::StateAsyncOptions)
}

/**
 * This hook function creates a state that is asynchronously computed using a suspend function.
 * It allows you to handle asynchronous data fetching or computation while maintaining a reactive state.
 * The state will be updated when the provided suspend function completes or when any of the dependency keys change.
 *
 * ```kotlin
 * // Basic usage with dependency tracking
 * val asyncState by useStateAsync(userId) {
 *     delay(1.seconds) // Simulate network request
 *     fetchUserData(userId) // Suspend function that returns data
 * }
 *
 * // With lazy loading and custom error handling
 * val asyncData by useStateAsync(dataId, optionsOf = {
 *     lazy = true // Only compute when the state is accessed
 *     onError = { error ->
 *         logger.error("Failed to load data", error)
 *     }
 * }) {
 *     fetchDataFromApi(dataId)
 * }
 * ```
 *
 * @param T The type of data to be computed asynchronously
 * @param keys Dependency values that will trigger recomputation when changed
 * @param initValue Optional initial value to use before the async computation completes
 * @param optionsOf Configuration options for the async state behavior
 * @param factory The suspend function that produces the state value
 * @return A State object containing the result of the async computation (or null if not yet computed or on error)
 */
@Composable
fun <T> useStateAsync(
    vararg keys: Any?,
    initValue: T? = null,
    optionsOf: StateAsyncOptions.() -> Unit = {},
    factory: suspend () -> T,
): State<T?> {
    val options by useCreation {
        StateAsyncOptions.optionOf(optionsOf)
    }
    val (lazy, onError) = options
    val (asyncRun) = useCancelableAsync()
    val (state, setState) = _useGetState(initValue)
    val calcFnRef = useLatestRef {
        asyncRun {
            try {
                setState(factory())
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    onError(e)
                    setState(null)
                }
            }
        }
    }
    if (lazy) {
        return useState(keys = keys) {
            calcFnRef.current()
            state.value
        }
    } else {
        useEffect(deps = keys) {
            calcFnRef.current()
        }
        return state
    }
}

/**
 * 这是一个可空的[useState]，如果对象的状态可能为空，应该使用它。
 *
 * This is a nullable [useState] and should be used if the object's state may be null.
 */
@Composable
fun <T> _useState(default: T): MutableState<T> = remember {
    mutableStateOf(default)
}
