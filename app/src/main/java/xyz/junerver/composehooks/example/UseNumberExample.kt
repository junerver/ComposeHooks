package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.random.Random
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author Junerver
  Date: 2024/3/11-8:34
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseNumberExample() {
    val (countInt, setCountInt) = useState(0)
    val (countLong, setCountLong) = useState(0L)
    val (countFloat, setCountFloat) = useState(0f)
    val (countDouble, setCountDouble) = useState(0.0)

    Surface {
        Column {
            Text(text = "countInt: $countInt")
            Text(text = "countLong: $countLong")
            Text(text = "countFloat: $countFloat")
            Text(text = "countDouble: $countDouble")

            TButton(text = "+ random") {
                setCountInt(countInt + Random.nextInt(10))
                setCountLong(countLong + Random.nextLong(10))
                setCountFloat(countFloat + Random.nextFloat())
                setCountDouble(countDouble + Random.nextDouble(10.0))
            }
        }
    }
}
