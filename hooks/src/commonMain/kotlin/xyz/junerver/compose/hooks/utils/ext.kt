package xyz.junerver.compose.hooks.utils

import androidx.compose.runtime.State
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.Ref

/*
  Description:
  Author: Junerver
  Date: 2024/8/1-10:44
  Email: junerver@gmail.com
  Version: v1.0
*/

internal val currentTime: Instant
    get() = Clock.System.now()

internal fun unwrap(deps: Array<out Any?>) = deps.map {
    when (it) {
        is State<*> -> it.value
        is Ref<*> -> it.current
        else -> it
    }
}.toTypedArray()
