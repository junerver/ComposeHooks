package xyz.junerver.composehooks

import android.os.Bundle
import android.os.Parcelable
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
import com.tencent.mmkv.MMKV
import xyz.junerver.compose.hooks.PersistentContext
import xyz.junerver.compose.hooks.notifyDefaultPersistentObserver
import xyz.junerver.compose.hooks.useredux.ReduxProvider
import xyz.junerver.composehooks.example.store
import xyz.junerver.composehooks.route.routes
import xyz.junerver.composehooks.route.subRequestRoutes
import xyz.junerver.composehooks.route.useNavigate
import xyz.junerver.composehooks.route.useRoutes
import xyz.junerver.composehooks.ui.theme.ComposeHooksTheme

val mmkv = MMKV.defaultMMKV()

fun mmkvSave(key: String, value: Any?) {
    when (value) {
        is Int -> mmkv.encode(key, value)
        is Long -> mmkv.encode(key, value)
        is Double -> mmkv.encode(key, value)
        is Float -> mmkv.encode(key, value)
        is Boolean -> mmkv.encode(key, value)
        is String -> mmkv.encode(key, value)
        is ByteArray -> mmkv.encode(key, value)
        is Parcelable -> mmkv.encode(key, value)
    }
    notifyDefaultPersistentObserver(key)
}

fun mmkvGet(key: String, value: Any): Any {
    return when (value) {
        is Int -> mmkv.decodeInt(key, value)
        is Long -> mmkv.decodeLong(key, value)
        is Double -> mmkv.decodeDouble(key, value)
        is Float -> mmkv.decodeFloat(key, value)
        is Boolean -> mmkv.decodeBool(key, value)
        is String -> mmkv.decodeString(key, value)
        is ByteArray -> mmkv.decodeBytes(key, value)
        is Parcelable -> mmkv.decodeParcelable(key, value.javaClass)
        else -> error("wrong type of default value！")
    } as Any
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeHooksTheme {
                // provide store for all components
                ReduxProvider(store = store) {
                    PersistentContext.Provider(
                        value = PersistentContext.LocalCtx.current.copy(
                            first = ::mmkvGet,
                            second = ::mmkvSave
                        )
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            useRoutes(routes = routes + subRequestRoutes)
                        }
                    }
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
        items(routes.entries.filter { it.key.startsWith("use") }) { (route, _) ->
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
