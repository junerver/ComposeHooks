package xyz.junerver.compose.hooks.useupdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import xyz.junerver.compose.hooks.Tuple1
import xyz.junerver.compose.hooks.useState

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-11:28
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("UNUSED_VARIABLE")
@Composable
fun useUpdateImpl(): () -> Unit {
    var state by useState(0.0)
    val (single) = Tuple1(state) // core logic
    return {
        state += 1
    }
}
