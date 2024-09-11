package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
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
    val ctx = LocalContext.current
    useEffect {
        // just like [useMount]
    }

    val (state, setState) = useGetState(0)
    useEffect(state) {
        // when deps change this block will executed
        ctx.toast("useEffect deps change")
    }
    Surface {
        Column {
            TButton(text = "+1") {
                setState(state + 1)
            }
        }
    }
}
