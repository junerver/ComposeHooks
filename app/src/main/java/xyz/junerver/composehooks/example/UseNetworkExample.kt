package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import xyz.junerver.compose.hooks.usenetwork.NetworkProvider
import xyz.junerver.compose.hooks.usenetwork.rememberNetwork

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-14:06
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseNetworkExample() {
    NetworkProvider {
        val networkState by rememberNetwork()
        Surface {
            Column {
                Text(text = "$networkState")
            }
        }
    }
}
