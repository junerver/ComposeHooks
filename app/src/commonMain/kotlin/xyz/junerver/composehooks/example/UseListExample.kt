package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlin.random.Random
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useListReduce
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-14:35
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseListExample() {
    val listState = useList(1, 2, 3)
    val listCount by useListReduce(listState, { a, b -> a + b })
    useEffect(listState) {
        println("list change!")
    }
    Surface {
        Column {
            Row {
                TButton(text = "+1") {
                    listState.add(listState.size + 1)
                }
                TButton(text = "-1") {
                    if (listState.isNotEmpty()) {
                        listState.removeLast()
                    }
                }
                TButton(text = "change random") {
                    val index = Random.nextInt(listState.lastIndex)
                    listState[index] = Random.nextInt()
                }
            }
            Text(text = "list size: ${listState.size}")
            Text(text = "list reduce result: $listCount")
            Text("The background color changes indicate that the component recompose")
            LazyColumn {
                items(listState) {
                    RandomItem(it)
                }
            }
        }
    }
}

@Composable
fun RandomItem(content: Int) {
    Row(modifier = Modifier.randomBackground()) {
        Text(text = "item content is : $content")
    }
}
