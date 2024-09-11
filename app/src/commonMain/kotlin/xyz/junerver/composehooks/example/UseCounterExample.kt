package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import arrow.core.left
import arrow.core.right
import xyz.junerver.compose.hooks.CounterOptions
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useCounter
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/7/8-13:53
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseCounterExample() {
    val (current, inc, dec, set, reset) = useCounter(
        initialValue = 100, // 即使配置了100也不会超出选项的上下限
        options = CounterOptions.optionOf {
            min = 1
            max = 10
        }
    )

    Surface {
        Column {
            Text(text = "$current [max: 10; min: 1;]")
            Row {
                TButton(text = "inc()") {
                    inc(1)
                }
                TButton(text = "dec()") {
                    dec(1)
                }
                TButton(text = "set(3)") {
                    /**
                     * 这里如果你想传入的是数值则使用 [left] 函数， 如果想传入的是函数 则使用 [right] 函数。
                     * 也可以手动导入[invoke]来优化使用体验。
                     *
                     * Here, if you want to pass in a numerical value, use the [left] function.
                     * If you want to pass in a function, use the [right] function. You
                     * can also manually import [invoke] to optimize the user experience.
                     *
                     * ```
                     * set(3.left())
                     * set({value:Int ->
                     *      value/3
                     * }.right())
                     * ```
                     */
                    set(3)
                }
                TButton(text = "set(/3)") {
                    set { value: Int -> value / 3 }
                }
                TButton(text = "reset()") {
                    reset()
                }
            }
        }
    }
}
