package xyz.junerver.composehooks.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-10:13
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun TButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(onClick = { onClick() }, enabled = enabled, modifier = modifier.padding(PaddingValues(4.dp))) {
        Text(text = text)
    }
}
