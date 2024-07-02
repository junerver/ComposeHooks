package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.text.DateFormat
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useNow
import xyz.junerver.composehooks.example.request.DividerSpacer

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-12:08
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseNowExample() {
    val now = useNow()
    val customize = useNow(
        optionsOf {
            format = {
                DateFormat.getDateInstance(DateFormat.FULL).format(it)
            }
        }
    )
    Surface {
        Column {
            Text(text = now)
            DividerSpacer()
            Text(text = customize)
        }
    }
}
