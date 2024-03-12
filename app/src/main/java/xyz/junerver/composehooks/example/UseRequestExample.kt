package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.composehooks.example.request.subRoutes
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/12-8:13
 * Email: junerver@gmail.com
 * Version: v1.0
 */
val SubNavContext = createContext<NavHostController?>(null)

@Composable
fun useSubNav() = useContext(context = SubNavContext)!!

@Composable
fun UseRequestExample() {
    Surface {
        SubNavContext.Provider(value = rememberNavController()) {
            val nav = useSubNav()
            NavHost(navController = nav, startDestination = "/") {
                subRoutes.map { route ->
                    composable(route.key) { route.value() }
                }
            }
        }
    }
}

@Composable
fun RequestExampleList() {
    val nav = useSubNav()
    Surface {
        Column {
            subRoutes.entries.filter { it.key != "/" }.map { (route, _) ->
                TButton(text = route) {
                    nav.navigate(route)
                }
            }
        }
    }
}
