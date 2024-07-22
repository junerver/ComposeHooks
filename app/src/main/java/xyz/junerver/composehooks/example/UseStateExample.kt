package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.tuple

/*
  Description: [useState]can make controlled components easier to create
  Author: Junerver
  Date: 2024/3/8-14:29
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseStateExample() {
    /*
     * Important note: While this method is convenient, it also has two flaws:
     * 1. It causes closure problems. When we use state in a closure function,
     *    closure problems will occur, need to use [useLatestRef] to avoid.
     * 2. When you call the set function quickly(millisecond level), Compose's
     *    recompose optimization will be triggered, resulting in state loss.
     */
    val (state, setState) = useState("")

    Surface {
        Column {
            Text(text = "this is a simple controlled component:")
            OutlinedTextField(value = state, onValueChange = setState)
            Text(text = "input：$state")
            Spacer(modifier = Modifier.height(20.dp))
            UseStateQuestionOne()
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            UseStateQuestionTwo()
            Spacer(modifier = Modifier.height(20.dp))
            HowToAvoidClosureProblems()
        }
    }
}

@Composable
private fun UseStateQuestionOne() {
    /** Direct use mutable state */
    val directState = useState(default = "direct")

    val (state, setState) = useState("state")

    val (state2, setState2, getState) = useGetState("useGetState")

    var byState by useState("by delegate")

    // When using destructuring declarations, you need to pay special attention to coroutine scenarios.
    LaunchedEffect(key1 = Unit) {
        repeat(10) {
            delay(1.seconds)
            directState.value += "."
            // Closure problems will occur when using the value of state in a closure function
            setState("$state.")
            // by delegate, it will not cause closure problems
            byState += "."
            // useState + useLatestRef ,Can avoid closure problems
            setState2("${getState()}.")
        }
    }
    Column {
        Text(text = "Question1. Closure problems")
        Text(text = state)
        Text(text = state2)
        Text(text = byState)
        Text(text = directState.value)
    }
}

@Composable
private fun UseStateQuestionTwo() {
    val directState = useState(default = "direct")

    /* useLatestRef 可以避免闭包问题 */
    val (state, setState) = useState("stateRef")
    val stateRef = useLatestRef(state)

    val (state2, setState2, getState) = useGetState("useGetState")

    var byState by useState("by delegate")
    LaunchedEffect(key1 = Unit) {
        repeat(20) {
            directState.value += "."
            // If you call the set function quickly(millisecond level), there will be a problem of state loss.
            setState("${stateRef.current}.")
            // if use by delegate, can modify status correctly
            byState += "."
            setState2(getState() + '.')
        }
    }
    Column {
        Text(text = "Question2. modify state very quickly")
        Text(text = state)
        Text(text = state2)
        Text(text = byState)
        Text(text = directState.value)
    }
}

@Composable
private fun HowToAvoidClosureProblems() {
    val (num, add) = useAddIncorrect(default = 0)
//    val (num, add) = useAddCorrect1(default = 0)
//    val (num, add) = useAddCorrect2(default = 0)
//    val (num, add) = useAddCorrect3(default = 0)
//    val (num, add) = useAddCorrect4(default = 0)
    Column {
        Text(text = "current: $num")
        TButton(text = "+1") {
            add()
        }
    }
}

/**
 * An example showing using destructuring declarations can lead to closure
 * problems in some cases
 *
 * @param default
 * @return
 */
@Composable
private fun useAddIncorrect(default: Int): Tuple2<Int, () -> Unit> {
    val (state, setState) = useState(default)

    // This kind of code will cause closure problems,
    fun add() {
        setState(state + 1) // The value of state is always default
    }
    return tuple(
        first = state,
        second = ::add
    )
}

@Composable
private fun useAddCorrect1(default: Int): Tuple2<Int, () -> Unit> {
    var state by _useState(default)

    // Using the `by` delegate can avoid most closure problems
    fun add() {
        state += 1
    }
    return tuple(
        first = state,
        second = ::add
    )
}

@Composable
private fun useAddCorrect2(default: Int): Tuple2<Int, () -> Unit> {
    val (state, setState) = _useState(default)
    // Using lambda generally does not cause closure problems in simple scenarios,
    // but if it is a complex lambda like `useReducer` source code, it may also cause
    // closure problems. This situation can be circumvented by `useLatestRef`.
    val add = { setState(state + 1) }
    return tuple(
        first = state,
        second = add
    )
}

/**
 * You can use [useGetState] to get the Ref of state using tuple's third
 * [get] fun
 *
 * @param default
 * @return
 */
@Composable
private fun useAddCorrect3(default: Int): Tuple2<Int, () -> Unit> {
    val (state, setState, getState) = useGetState(default)
    fun add() {
        setState(getState() + 1)
    }
    return tuple(
        first = state,
        second = ::add
    )
}

@Composable
private fun useAddCorrect4(default: Int): Tuple2<Int, () -> Unit> {
    val (state, setState) = useState(default)
    val stateRef = useLatestRef(state)
    fun add() {
        setState(stateRef.current + 1)
    }
    return tuple(
        first = state,
        second = ::add
    )
}
