package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.random.Random
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/11-11:36
  Email: junerver@gmail.com
  Version: v1.0
*/
data class Subject(val flag: String) {
    init {
        println("UseCreationExample Subject is be instantiated：$flag")
    }
}

@Composable
fun UseCreationExample() {
    /**
     * 当组件刷新时，[useRef]会创建一个*一次性*的实例，这个可能会带来一些性能问题；
     *
     * When the component is refreshed, [useRef] will create a one-time instance, which may cause some performance issues;
     */
    val ref = useRef(default = Subject("useRef${Random.nextDouble()}"))
    val creRef = useCreation {
        Subject("useCreation${Random.nextDouble()}")
    }
    val update = useUpdate()
    Surface {
        Column {
            Text(text = ref.current.flag)
            Text(text = creRef.current.flag)
            TButton(text = "update and see log") {
                update()
            }
        }
    }
}
