package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlin.math.abs

/*
  Description:
  Author: Junerver
  Date: 2025/7/3-19:42
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useAbs(number: Number): State<Number> = useState(number) {
    when (number) {
        is Int -> abs(number)
        is Double -> abs(number)
        is Float -> abs(number)
        is Long -> abs(number)
        else -> number
    }
}
