package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.random.Random
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-14:47
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseMapExample(){
    val mapState = useMap(
        1 to "first",
        2 to "second"
    )
    Surface {
        Column {
            Row {
                TButton(text = "+1") {
                    mapState[mapState.size+1] = "random${Random.nextInt()}"
                }
                TButton(text = "-1") {
                    if (mapState.isNotEmpty()) {
                        mapState.remove(mapState.map { it.key }.last())
                    }
                }
                TButton(text = "change") {
                    mapState[mapState.map { it.key }.random()] = "chang:${Random.nextInt()}"
                }
            }

            mapState.map {
                Text(text = "${it.key} : ${it.value}")
            }
        }
    }
}
