package xyz.junerver.composehooks.route

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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

@SuppressLint("ComposableNaming")
@Composable
fun useRoutes(routes: Map<String, Component>) {
    val navController = rememberNavController()
    return CompositionLocalProvider(LocalNavHostController provides navController) {
        NavHost(navController = navController, startDestination = "/") {
            routes.map { route ->
                composable(route.key) { route.value() }
            }
        }
    }
}
