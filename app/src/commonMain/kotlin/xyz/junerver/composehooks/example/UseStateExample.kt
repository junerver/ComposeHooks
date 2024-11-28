package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.Tuple2
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.example.request.DividerSpacer
import xyz.junerver.composehooks.ui.component.TButton

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
            DividerSpacer()
            Text(
                "Don't destructure `useState`, it will cause the following problems：",
                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
            )
            UseStateQuestionOne()
            Spacer(modifier = Modifier.height(20.dp))
            DividerSpacer()
            Spacer(modifier = Modifier.height(20.dp))
            UseStateQuestionTwo()
            DividerSpacer()
            Text("Demonstrates how to avoid closure problems, please see the sample code")
            HowToAvoidClosureProblems()
        }
    }
}

@Composable
private fun UseStateQuestionOne() {
    val (state, setState) = useState("destructure State")

    /** Direct use mutable state */
    val directState = useState(default = "directly read and write the MutableState value")

    var byState by useState("by delegate")

    val (state2, setState2) = useGetState("useGetState")

    // When using destructuring declarations, you need to pay special attention to coroutine scenarios.
    LaunchedEffect(key1 = Unit) {
        repeat(10) {
            delay(1.seconds)
            // Closure issues occur when using destructured state values in closure functions
            setState("$state.")
            // Directly using MutableState to access the state value will not cause closure problems
            directState.value += "."
            // by delegate, it will not cause closure problems
            byState += "."
            // useState + useLatestRef ,Can avoid closure problems
            setState2 { "$it." }
        }
    }
    Column {
        Text(text = "Question1. Closure problems")
        Text(text = state)
        Text(text = directState.value)
        Text(text = byState)
        Text(text = state2.value)
    }
}

/**
 * 如果我们直接对 [MutableState] 进行解构，在快速更新状态的场景会导致状态不更新；
 * 解决方法：
 * 1. 使用 `by` 委托
 * 2. 直接使用 [MutableState.value]
 * 3. 使用 [useGetState]
 */
@Composable
private fun UseStateQuestionTwo() {
    // useLatestRef 可以避免闭包问题
    val (state, setState) = useState("destructure State")
    val stateRef = useLatestRef(state)

    val directState = useState(default = "directly read and write the MutableState value")

    var byState by useState("by delegate")

    val (state2, setState2) = useGetState("useGetState")

    LaunchedEffect(key1 = Unit) {
        repeat(20) {
            // If you call the set function quickly(millisecond level), there will be a problem of state loss.
            setState("${stateRef.current}.")
            directState.value += "."
            // if use by delegate, can modify status correctly
            byState += "."
            setState2 { "$it." }
        }
    }
    Column {
        Text(text = "Question2. modify state very quickly")
        Text(text = state)
        Text(text = directState.value)
        Text(text = byState)
        Text(text = state2.value)
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
    val (state, setState) = useGetState(default)

    fun add() {
        setState { it + 1 }
    }
    return tuple(
        first = state.value,
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
