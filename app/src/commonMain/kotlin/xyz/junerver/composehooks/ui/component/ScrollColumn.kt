package xyz.junerver.composehooks.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/*
  Description:
  Author: Junerver
  Date: 2024/12/3-19:12
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun ScrollColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxSize().verticalScroll(scrollState)) {
        content.invoke()
    }
}
