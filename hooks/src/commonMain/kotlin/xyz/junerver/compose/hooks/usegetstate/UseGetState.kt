package xyz.junerver.compose.hooks.usegetstate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.GetValueFn
import xyz.junerver.compose.hooks.SetterEither
import xyz.junerver.compose.hooks.SetValueFn
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useState

/*
  Description: You can get the latest state in the callback
  Author: Junerver
  Date: 2024/2/23-8:41
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useGetStateImpl(default: T & Any): GetStateHolder<T & Any> {
    val state = useState(default)
    return remember {
        GetStateHolder(
            state = state,
            setValue = { value: SetterEither<T & Any> ->
                val newValue = value.fold({ it }, { it(state.value) })
                state.value = newValue
            },
            getValue = { state.value },
        )
    }
}

@Composable
fun <T> _useGetStateImpl(default: T): GetStateHolder<T> {
    val state = _useState(default)
    return remember {
        GetStateHolder(
            state = state,
            setValue = { value: SetterEither<T> ->
                val newValue = value.fold({ it }, { it(state.value) })
                state.value = newValue
            },
            getValue = { state.value },
        )
    }
}

@Stable
data class GetStateHolder<T>(
    val state: State<T>,
    val setValue: SetValueFn<SetterEither<T>>,
    val getValue: GetValueFn<T>,
)
