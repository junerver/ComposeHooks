package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.Padding
import xyz.junerver.compose.hooks.useNow
import xyz.junerver.composehooks.example.request.DividerSpacer
import xyz.junerver.composehooks.utils.CHINESE_FULL
import xyz.junerver.composehooks.utils.toLocalDateTime
import xyz.junerver.composehooks.utils.tsMs

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-12:08
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseNowExample() {
    val now by useNow()
    val customize by useNow(
        optionsOf = {
            format = {
                it.tsMs
                    .toLocalDateTime()
                    .format(
                        LocalDateTime.Format {
                            year()
                            chars("年")
                            monthNumber()
                            chars("月")
                            day(padding = Padding.ZERO)
                            chars("日")
                            dayOfWeek(DayOfWeekNames.CHINESE_FULL)
                        },
                    )
            }
        },
    )
    Surface {
        Column {
            Text(text = now)
            DividerSpacer()
            Text(text = customize)
        }
    }
}
