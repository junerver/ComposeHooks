package xyz.junerver.compose.hooks.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import org.jetbrains.annotations.Nullable
import xyz.junerver.compose.hooks.SetValueFn
import xyz.junerver.compose.hooks._useState
import xyz.junerver.kotlin.Tuple2

/*
  Description: 目前仅在内部使用，用来返回State本身与set函数
  Author: Junerver
  Date: 2024/7/22-7:38
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
internal fun <T> _useSetState(@Nullable default: T): Tuple2<State<T>, SetValueFn<T>> {
    val state = _useState(default = default)
    return Tuple2(
        first = state,
        second = { state.value = it }
    )
}
