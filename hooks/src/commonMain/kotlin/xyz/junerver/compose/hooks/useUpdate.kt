package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import xyz.junerver.kotlin.Single

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-11:28
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("UNUSED_VARIABLE")
@Composable
inline fun useUpdate(): () -> Unit {
    var state by useState(0.0)
    val (single) = Single(state) // core logic
    return {
        state += 1
    }
}
