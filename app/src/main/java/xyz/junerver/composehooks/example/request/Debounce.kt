package xyz.junerver.composehooks.example.request

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

/*
  Description:
  Author: Junerver
  Date: 2024/3/13-14:28
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun Debounce() {
    Surface {
        Container("debounced", OptionFunc.Debounce)
    }
}
