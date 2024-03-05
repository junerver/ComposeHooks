package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.tuple

/**
 * Description:
 * @author Junerver
 * date: 2024/1/26-14:21
 * Email: junerver@gmail.com
 * Version: v1.0
 */
// reducer 函数类型抽象
typealias Reducer<S> = (prevState: S, action: Action) -> S

typealias Dispatch = (Action) -> Unit

abstract class Action(open val type: Any, open val payload: Any?)

fun Pair<Any, Any?>.asAction(): Action = object : Action(this.first, this.second) {}

fun Any.asAction(): Action = (this to null).asAction()

@Composable
inline fun <reified S> useReducer(
    noinline reducer: Reducer<S>,
    initialState: S
): Tuple2<S, Dispatch> {
    val (state, setState) = useState(initialState)
    return tuple(
        first = state,  //state状态值
        second = { action: Action -> setState(reducer(state, action)) } //dispatch函数
    )
}
