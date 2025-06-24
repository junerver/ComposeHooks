package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.utils.currentTime

/*
  Description: Records the [Instant] of the last change
  Author: Junerver
  Date: 2025/6/24-16:09
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun useLastChanged(source: Any?): State<Instant> {
    val (lastChanged, setLastChanged) = useGetState<Instant>(currentTime)
    useEffect(source) {
        setLastChanged(currentTime)
    }
    return lastChanged
}
