package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/*
  Description: A hook to conveniently manage Boolean state
  Author: Junerver
  Date: 2024/1/26-13:38
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useBoolean(default: Boolean = false): BooleanHolder {
    val (state, setState, getState) = useGetState(default)
    return remember {
        BooleanHolder(
            state = state, // boolean state
            toggle = { setState(!getState()) }, // toggle fun
            setValue = { b: Boolean -> setState(b) }, // set fun
            setTrue = { setState(true) }, // setTrue
            setFalse = { setState(false) } // setFalse
        )
    }
}

@Stable
data class BooleanHolder(
    val state: State<Boolean>,
    val toggle: ToggleFn,
    val setValue: SetValueFn<Boolean>,
    val setTrue: SetTrueFn,
    val setFalse: SetFalseFn,
)
