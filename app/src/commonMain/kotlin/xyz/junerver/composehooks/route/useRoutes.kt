package xyz.junerver.composehooks.route

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xyz.junerver.composehooks.getPlatform
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-9:24
  Email: junerver@gmail.com
  Version: v1.0
*/
typealias Component = @Composable () -> Unit

private val LocalNavHostController: ProvidableCompositionLocal<NavHostController> =
    staticCompositionLocalOf { error("provide first") }

@Composable
fun useNavigate() = LocalNavHostController.current

@Composable
fun useRoutes(routes: Map<String, Component>) {
    val navController = rememberNavController()
    return CompositionLocalProvider(LocalNavHostController provides navController) {
        Column {
            if (getPlatform().name.startsWith("Java")) {
                TButton("back") {
                    navController.popBackStack()
                }
            }
            NavHost(navController = navController, startDestination = "/") {
                routes.map { route ->
                    composable(route.key) { route.value() }
                }
            }
        }
    }
}
