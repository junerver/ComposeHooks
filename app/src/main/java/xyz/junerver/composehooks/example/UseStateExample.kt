package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useState

/**
 * Description: [useState]can make controlled components easier to create
 * @author Junerver
 * date: 2024/3/8-14:29
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseStateExample() {
    /**
     * Important note: While this method is convenient, it also has two flaws:
     * 1. It causes closure problems. When we use state in a closure function,
     *  closure problems will occur, need to use [useLatestRef] to avoid.
     * 2. When you call the set function quickly(millisecond level),
     *  Compose's recompose optimization will be triggered, resulting in state loss.
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
            UseStateQuestionTwo()
        }
    }
}

@Composable
fun UseStateQuestionOne() {
    val (state, setState) = useState("state")

    val (state2, setState2) = useState("stateRef")
    val state2Ref = useLatestRef(state2)

    var remState by remember {
        mutableStateOf("remember")
    }
    LaunchedEffect(key1 = Unit) {
        repeat(10) {
            delay(1.seconds)
            // Closure problems will occur when using the value of state in a closure function
            setState("$state.")
            // by + remember, it will not cause closure problems
            remState += "."
            // useState + useLatestRef ,Can avoid closure problems
            setState2("${state2Ref.current}.")
        }
    }
    Column {
        Text(text = "Question1.")
        Text(text = state)
        Text(text = state2)
        Text(text = remState)
    }
}

@Composable
fun UseStateQuestionTwo() {
    val (state2, setState2) = useState("stateRef")
    val state2Ref = useLatestRef(state2)
    var remState by remember {
        mutableStateOf("remember")
    }
    LaunchedEffect(key1 = Unit) {
        repeat(20) {
            remState += "."
            // If you call the set function quickly(millisecond level), there will be a problem of state loss.
            setState2("${state2Ref.current}.")
        }
    }
    Column {
        Text(text = "Question2.")
        Text(text = state2)
        Text(text = remState)
    }
}
