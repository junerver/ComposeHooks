package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.junerver.composehooks.route.subRequestRoutes
import xyz.junerver.composehooks.route.useNavigate
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * Author: Junerver
 * Date: 2024/3/12-8:13
 * Email: junerver@gmail.com
 * Version: v1.0
 */

@Composable
fun RequestExampleList() {
    val nav = useNavigate()
    val scrollState = rememberScrollState()
    Surface {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            subRequestRoutes.entries.map { (route, _) ->
                TButton(text = route) {
                    nav.navigate(route)
                }
            }
        }
    }
}
