package xyz.junerver.composehooks.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
  Description:
  Author: Junerver
  Date: 2025/7/16-18:26
  Email: junerver@gmail.com
  Version: v1.0
*/


@Composable
fun DividerSpacer(
    top: Dp = 20.dp,
    bottom: Dp = 20.dp
) {
    HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 20.dp))
}
