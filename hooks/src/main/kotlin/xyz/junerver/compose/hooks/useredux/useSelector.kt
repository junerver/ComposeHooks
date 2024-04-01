package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useContext

/**
 * Description:
 * @author Junerver
 * @date: 2024/4/1-14:46
 * @Email: junerver@gmail.com
 * @Version: v1.0
 */
@Composable
inline fun <reified T> useSelector(): T {
    val map = useContext(context = ReduxContext)
    return map.first[T::class] as T
}
