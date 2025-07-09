package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import xyz.junerver.compose.hooks.useToggle
import xyz.junerver.compose.hooks.useToggleEither
import xyz.junerver.compose.hooks.useToggleVisible
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/11-9:19
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseToggleExample() {
    val (state, toggle) = useToggle("hello", "world")
    val (either, toggleEither) = useToggleEither("example", Random.nextDouble())
    val (component, toggleVisible) = useToggleVisible {
        Text(text = "a simple component can be visible/invisible")
    }

    Surface {
        Column {
            Text(text = "current: $state")
            TButton(text = "toggle") {
                toggle()
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "either: ${
                    either.fold(
                        { "string: $it" },
                        { "double: $it" },
                    )
                }",
            )
            TButton(text = "toggle") {
                toggleEither()
            }
            Spacer(modifier = Modifier.height(10.dp))
            component()
            TButton(text = "toggle") {
                toggleVisible()
            }
        }
    }
}
