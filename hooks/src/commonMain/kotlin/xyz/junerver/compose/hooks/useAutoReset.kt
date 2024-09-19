package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import kotlin.time.Duration
import kotlinx.coroutines.delay

/*
  Description: A hook which will reset state to the default value after some time.
  Author: Junerver
  Date: 2024/7/8-15:52
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useAutoReset(default: T & Any, interval: Duration): MutableState<T & Any> {
    val state = useState(default = default)
    val defaultValue = useCreation { default }
    useEffect(state) {
        delay(interval)
        state.value = defaultValue.current
    }
    return state
}
