package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlin.random.Random
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useImmutableList
import xyz.junerver.compose.hooks.useImmutableListReduce
import xyz.junerver.composehooks.ui.component.TButton

/**
 * @Author Junerver
 * @Date 2024/9/27-19:44
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description
 */

@Composable
fun UseImmutableListExample() {
    val immutableListHolder = useImmutableList(1, 2, 3)
    val immutableList by immutableListHolder.list
    val listCount by useImmutableListReduce(immutableList, { a, b -> a + b })
    useEffect(immutableList) {
        println("list change")
    }

    Surface {
        Column {
            Row {
                TButton(text = "+1") {
                    immutableListHolder.mutate {
                        it.add(immutableList.size + 1)
                    }
                }
                TButton(text = "-1") {
                    immutableListHolder.mutate {
                        it.removeLast()
                    }
                }
                TButton(text = "change") {
                    val index = Random.nextInt(immutableList.lastIndex)
                    immutableListHolder.mutate {
                        it[index] = Random.nextInt()
                    }
                }
            }
            Text(text = "list reduce result: $listCount")
            LazyColumn {
                items(immutableList) {
                    RandomItem(it)
                }
            }
        }
    }
}
