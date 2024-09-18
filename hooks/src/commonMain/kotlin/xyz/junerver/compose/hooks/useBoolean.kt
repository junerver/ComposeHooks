package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import xyz.junerver.kotlin.Tuple5
import xyz.junerver.kotlin.tuple

/*
  Description: A hook to conveniently manage Boolean state
  Author: Junerver
  Date: 2024/1/26-13:38
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useBoolean(default: Boolean = false): Tuple5<State<Boolean>, ToggleFn, SetValueFn<Boolean>, SetTrueFn, SetFalseFn> {
    val (state, setState, getState) = useGetState(default)
    return remember {
        tuple(
            first = state, // boolean state
            second = { setState(!getState()) }, // toggle fun
            third = { b: Boolean -> setState(b) }, // set fun
            fourth = { setState(true) }, // setTrue
            fifth = { setState(false) } // setFalse
        )
    }
}
