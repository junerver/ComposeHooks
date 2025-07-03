package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import xyz.junerver.compose.hooks.useCycleList
import xyz.junerver.composehooks.ui.component.SimpleContainer
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2025/7/3-17:50
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseCycleListExample() {
    val (state, index, next, prev) = useCycleList(
        persistentListOf(
            "Dog",
            "Cat",
            "Lizard",
            "Shark",
            "Whale",
            "Dolphin",
            "Octopus",
            "Seal"
        )
    ) {
        initialValue = "Lizard1"
        fallbackIndex = 100
    }
    Surface {
        Column(modifier = Modifier.randomBackground()) {
            SimpleContainer { Text(text = "Current: ${state.value}") }
            SimpleContainer { Text(text = "Current Index: ${index.value}") }
            SimpleContainer {
                Row {
                    TButton("Prev", onClick = { prev() })
                    TButton("Next", onClick = { next() })
                }
            }
        }
    }
}
