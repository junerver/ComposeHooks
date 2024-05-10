package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import kotlin.random.Random

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-11:28
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Suppress("UNUSED_VARIABLE")
@Composable
fun useUpdate(): () -> Unit {
    val (state, setState) = useGetState(0.0)
    return fun() {
        setState(Random.nextDouble())
    }
}
