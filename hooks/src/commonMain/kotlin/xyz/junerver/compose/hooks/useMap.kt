package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap

/*
  Description: More convenient to use dynamic Map state
  Author: Junerver
  Date: 2024/3/7-15:02
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for creating and managing dynamic map state in Compose.
 *
 * This hook provides a convenient way to create a [SnapshotStateMap] from a vararg list
 * of key-value pairs. It's particularly useful when you need to maintain a mutable map
 * that can trigger recompositions when modified.
 *
 * @param pairs The initial key-value pairs as varargs
 * @return A [SnapshotStateMap] containing the key-value pairs
 *
 * @example
 * ```kotlin
 * val map = useMap(
 *     "key1" to "value1",
 *     "key2" to "value2"
 * )
 *
 * // Add a key-value pair
 * map["key3"] = "value3"
 *
 * // Remove a key-value pair
 * map.remove("key1")
 *
 * // Update a value
 * map["key2"] = "newValue"
 *
 * // Clear the map
 * map.clear()
 * ```
 */
@Composable
fun <K, V> useMap(vararg pairs: Pair<K, V>): SnapshotStateMap<K, V> = remember {
    mutableStateMapOf(*pairs)
}

/**
 * A hook for creating and managing dynamic map state in Compose.
 *
 * This overload allows you to create a [SnapshotStateMap] from an iterable collection
 * of key-value pairs. It's useful when you have an existing collection of pairs that
 * you want to convert into a state map.
 *
 * @param pairs The initial key-value pairs as an iterable collection
 * @return A [SnapshotStateMap] containing the key-value pairs
 *
 * @example
 * ```kotlin
 * val initialPairs = listOf(
 *     "key1" to "value1",
 *     "key2" to "value2"
 * )
 *
 * val map = useMap(initialPairs)
 *
 * // Add a key-value pair
 * map["key3"] = "value3"
 *
 * // Remove a key-value pair
 * map.remove("key1")
 *
 * // Update a value
 * map["key2"] = "newValue"
 *
 * // Clear the map
 * map.clear()
 * ```
 */
@Composable
fun <K, V> useMap(pairs: Iterable<Pair<K, V>>): SnapshotStateMap<K, V> = remember {
    pairs.toMutableStateMap()
}
