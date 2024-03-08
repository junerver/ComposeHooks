package xyz.junerver.composehooks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.junerver.composehooks.route.routes
import xyz.junerver.composehooks.route.useNavigate
import xyz.junerver.composehooks.route.useRoutes
import xyz.junerver.composehooks.ui.theme.ComposeHooksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeHooksTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    useRoutes(routes = routes)
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val nav = useNavigate()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        // 整体内边距
        contentPadding = PaddingValues(8.dp, 8.dp),
        // item 和 item 之间的纵向间距
        verticalItemSpacing = 4.dp,
        // item 和 item 之间的横向间距
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(routes.filter { it.first.startsWith("use") }) { (route, _) ->
            Button(onClick = { nav.navigate(route) }) {
                Text(text = route, maxLines = 1)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeHooksTheme {
        HomeScreen()
    }
}
