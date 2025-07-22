package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import xyz.junerver.composehooks.route.routes
import xyz.junerver.composehooks.route.useNavigate

/*
  Description:
  Author: Junerver
  Date: 2024/9/9-15:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun HomeScreen() {
    val nav = useNavigate()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp, 8.dp),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(routes.entries.filter { !it.key.contains("/") }) { (route, _) ->
            Button(onClick = { nav.navigate(route) }) {
                Text(text = route, maxLines = 1)
            }
        }
    }
}
