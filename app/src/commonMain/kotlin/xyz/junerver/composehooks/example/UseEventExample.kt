package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.random.Random
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useEventPublish
import xyz.junerver.compose.hooks.useEventSubscribe
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.composehooks.example.request.DividerSpacer
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/13-9:13
  Email: junerver@gmail.com
  Version: v1.0
 */
@Composable
fun UseEventExample() {
    Surface {
        Container()
    }
}

@Composable
fun Container() {
    val post = useEventPublish<Unit>()
    ScrollColumn {
        TButton("refresh") {
            post(Unit)
        }
        (0..10).map {
            SubComponent(index = it)
        }
    }
}

@Composable
fun SubComponent(index: Int) {
    val (state, setState) = useGetState(0.0)

    fun refresh() {
        setState(Random.nextDouble())
    }
    useEventSubscribe { _: Unit ->
        refresh()
    }
    Column {
        Row {
            Text(text = "index $index: ${state.value}")
            TButton(text = "refresh") {
                refresh()
            }
        }
        DividerSpacer()
    }
}
