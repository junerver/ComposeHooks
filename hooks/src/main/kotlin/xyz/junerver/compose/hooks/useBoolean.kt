package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import xyz.junerver.kotlin.Tuple5
import xyz.junerver.kotlin.tuple

/**
 * Description:
 * @author Junerver
 * date: 2024/1/26-13:38
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun useBoolean(default: Boolean = false): Tuple5<Boolean, () -> Unit, (Boolean) -> Unit, () -> Unit, () -> Unit> {
    val (state, setState) = useState(default)
    return tuple(
        first = state, // boolean状态
        second = { setState(!state) }, // toggle函数
        third = { b: Boolean -> setState(b) }, // set函数
        fourth = { setState(true) }, // setTrue
        fifth = { setState(false) } // setFalse
    )
}
