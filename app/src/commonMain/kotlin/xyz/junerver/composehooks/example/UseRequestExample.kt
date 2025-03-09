package xyz.junerver.composehooks.example

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import xyz.junerver.composehooks.route.subRequestRoutes
import xyz.junerver.composehooks.route.useNavigate
import xyz.junerver.composehooks.ui.component.ScrollColumn
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
    Surface {
        ScrollColumn {
            subRequestRoutes.entries.map { (route, _) ->
                TButton(text = route) {
                    nav.navigate(route)
                }
            }
        }
    }
}
