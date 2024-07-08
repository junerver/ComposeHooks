package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.jetbrains.annotations.Nullable

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
 * val (state,setState) = useState<Boolean?>(null)
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
fun <T> useState(default: T & Any): MutableState<T & Any> {
    return when (default) {
        is Int -> useInt(default)
        is Float -> useFloat(default)
        is Double -> useDouble(default)
        is Long -> useLong(default)
        else -> _useState(default)
    } as MutableState<T & Any>
}

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
fun <T> useState(vararg keys: Any, factory: () -> T): State<T> = remember(keys = keys) {
    derivedStateOf(factory)
}

/**
 * 这是一个可空的[useState]，如果对象的状态可能为空，应该使用它。
 *
 * This is a nullable [useState] and should be used if the object's state may be null.
 */
@Composable
fun <T> _useState(@Nullable default: T): MutableState<T> = remember {
    mutableStateOf(default)
}
