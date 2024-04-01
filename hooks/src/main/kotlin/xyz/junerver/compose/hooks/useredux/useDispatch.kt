package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.useContext

/**
 * Description:
 * @author Junerver
 * @date: 2024/4/1-14:46
 * @Email: junerver@gmail.com
 * @Version: v1.0
 */
@Composable
inline fun <reified A> useDispatch(): Dispatch<A> {
    val map = useContext(context = ReduxContext)
    return map.second[A::class] as Dispatch<A>
}
