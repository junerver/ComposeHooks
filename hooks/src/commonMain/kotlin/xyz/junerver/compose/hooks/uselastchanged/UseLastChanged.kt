package xyz.junerver.compose.hooks.uselastchanged

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlin.time.Instant
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.usecreation.useCreationImpl
import xyz.junerver.compose.hooks.usegetstate.useGetStateImpl
import xyz.junerver.compose.hooks.useupdateeffect.useUpdateEffectImpl
import xyz.junerver.compose.hooks.utils.currentInstant

/*
  Description: Records the [Instant] of the last change
  Author: Junerver
  Date: 2025/6/24-16:09
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun useLastChangedImpl(source: Any?): State<Instant> {
    val initialInstant = useCreationImpl { currentInstant }.current
    val (lastChanged, setLastChanged) = useGetStateImpl(initialInstant)
    useUpdateEffectImpl(source) {
        setLastChanged(currentInstant)
    }
    return lastChanged
}
