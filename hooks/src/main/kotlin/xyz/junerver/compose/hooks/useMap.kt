package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap

/**
 * Description:
 * @author Junerver
 * date: 2024/3/7-15:02
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun <K, V> useMap(vararg pairs: Pair<K, V>): SnapshotStateMap<K, V> = remember {
    mutableStateMapOf(*pairs)
}

@Composable
fun <K, V> useMap(pairs: Iterable<Pair<K, V>>): SnapshotStateMap<K, V> = remember {
    pairs.toMutableStateMap()
}
