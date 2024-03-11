package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.random.Random
import xyz.junerver.compose.hooks.useDouble
import xyz.junerver.compose.hooks.useFloat
import xyz.junerver.compose.hooks.useInt
import xyz.junerver.compose.hooks.useLong
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/11-8:34
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseNumberExample() {
    val (countInt, setCountInt) = useInt(0)
    val (countLong, setCountLong) = useLong(0)
    val (countFloat, setCountFloat) = useFloat(0f)
    val (countDouble, setCountDouble) = useDouble(0.0)

    Surface {
        Column {
            Text(text = "countInt: $countInt")
            Text(text = "countLong: $countLong")
            Text(text = "countFloat: $countFloat")
            Text(text = "countDouble: $countDouble")

            TButton(text = "+1") {
                setCountInt(countInt + Random.nextInt(10))
                setCountLong(countLong + Random.nextLong(10))
                setCountFloat(countFloat + Random.nextFloat())
                setCountDouble(countDouble + Random.nextDouble(10.0))
            }
        }
    }
}
