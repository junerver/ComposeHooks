package xyz.junerver.composehooks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.junerver.compose.hooks.useredux.ReduxProvider
import xyz.junerver.compose.hooks.useredux.plus
import xyz.junerver.composehooks.example.fetchStore
import xyz.junerver.composehooks.example.simpleStore
import xyz.junerver.composehooks.route.otherSubRoutes
import xyz.junerver.composehooks.route.routes
import xyz.junerver.composehooks.route.subRequestRoutes
import xyz.junerver.composehooks.route.useRoutes
import xyz.junerver.composehooks.ui.theme.ComposeHooksTheme

/*
  Description:
  Author: Junerver
  Date: 2024/9/9-15:37
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun App() {
    ComposeHooksTheme {
        // provide store for all components
        ReduxProvider(store = simpleStore + fetchStore) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                useRoutes(routes = routes + subRequestRoutes + otherSubRoutes)
            }
        }
    }
}
