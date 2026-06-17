@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key

@Composable
fun rememberKeyPress(key: Key, eventHandler: () -> Unit) = useKeyPress(key, eventHandler)

@Composable
fun rememberKeyPress(key1: Key, key2: Key, eventHandler: () -> Unit) = useKeyPress(key1, key2, eventHandler)

@Composable
fun rememberKeyPress(
    key1: Key,
    key2: Key,
    key3: Key,
    eventHandler: () -> Unit,
) = useKeyPress(key1, key2, key3, eventHandler)

@Composable
fun rememberKeyPress(
    key1: Key,
    key2: Key,
    key3: Key,
    key4: Key,
    eventHandler: () -> Unit,
) = useKeyPress(key1, key2, key3, key4, eventHandler)
