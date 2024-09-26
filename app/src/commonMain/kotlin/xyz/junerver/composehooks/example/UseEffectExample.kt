package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/11-11:09
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseEffectExample() {
//    val ctx = LocalContext.current
    useEffect {
        // just like [useMount]
        println("no dependencies are set, only execute once")
    }

    val (state, setState, getState) = useGetState(0)
    useEffect(state) {
        // when deps change this block will execute
        println("useEffect: state deps change")
    }

    val ref = useRef(0)

    useEffect(ref) {
        // when deps change this block will execute
        println("useEffect: ref deps change")
    }

    Surface {
        Column {
            Text("state deps: ${getState()}")
            TButton(text = "+1") {
                setState(state.value + 1)
            }

            Text("ref deps: ${ref.current}")
            TButton(text = "ref +1") {
                ref.current += 1
            }
        }
    }
}
