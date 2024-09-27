package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

/*
  Description: More convenient to use dynamic List state
  Author: Junerver
  Date: 2024/3/7-15:02
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useList(elements: Collection<T>): SnapshotStateList<T> = remember {
    elements.toMutableStateList()
}

@Composable
fun <T> useList(vararg elements: T): SnapshotStateList<T> = remember {
    mutableStateListOf(*elements)
}
