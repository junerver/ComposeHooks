package xyz.junerver.composehooks.ui.component

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/*
  Description:
  Author: Junerver
  Date: 2025/7/16-14:40
  Email: junerver@gmail.com
  Version: v1.0
*/

val colors = arrayOf(
    0x5D000000,
    0x5Dfef200,
    0x5Db5e51d,
    0x5D9ad9ea,
    0x5Dc3c3c3,
    0x5Dff7f26,
    0x5D23b14d,
    0x5D00a3e8,
    0x5D7f7f7f,
    0x5Dfeaec9,
    0x5Dc7bfe8,
    0x5Da349a3,
    0x5Dffffff,
    0x5Ded1b24,
    0x5D7092bf,
    0x5D3f47cc,
)

@Composable
fun Modifier.randomBackground(): Modifier = composed {
    this.background(Color(colors[Random.nextInt(colors.size)]))
}
