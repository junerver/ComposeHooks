package xyz.junerver.compose.hooks.useselectable
import xyz.junerver.compose.hooks.usestate.useStateImpl
import xyz.junerver.compose.hooks.usemap.useMapImpl
import xyz.junerver.compose.hooks.usecreation.useCreationImpl
import xyz.junerver.compose.hooks.SelectionMode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import xyz.junerver.compose.hooks.useeffect.useEffectImpl

/*
  Description: A utility function to help implement select or multi select feature.
  Author: kk
  Date: 2024/3/23-9:30
  Email: kkneverlie@gmail.com
  Version: v1.0
*/
@Composable
fun <KEY, ITEM> useSelectableImpl(
    selectionMode: SelectionMode<KEY>,
    items: List<ITEM>,
    keyProvider: (ITEM) -> KEY,
): SelectableHolder<KEY, ITEM> {
    val currentSelectionMode = useIdentityState(selectionMode)
    val currentItems = useIdentityState(items)
    val currentKeyProvider = useIdentityState(keyProvider)
    val initialMap = useCreationImpl {
        items.map { item ->
            val key = keyProvider(item)
            key to selectionMode.getInitialSelected(key)
        }
    }
    val selectedMap = useMapImpl(pairs = initialMap.current)
    val currentKeys = items.map(keyProvider)

    useEffectImpl(currentKeys, selectionMode is SelectionMode.SingleSelect<*>) {
        val activeKeys = currentKeys.toSet()
        Snapshot.withMutableSnapshot {
            selectedMap.keys.toList().filterNot(activeKeys::contains).forEach(selectedMap::remove)
            currentKeys.forEach { key ->
                if (!selectedMap.containsKey(key)) {
                    selectedMap[key] = currentSelectionMode.value.getInitialSelected(key)
                }
            }
            if (currentSelectionMode.value is SelectionMode.SingleSelect<*>) {
                val selectedKey = currentKeys.firstOrNull { selectedMap[it] == true }
                currentKeys.forEach { key -> selectedMap[key] = key == selectedKey }
            }
        }
    }

    val selectedKeys = useStateImpl { selectedMap.filterValues { it }.keys }
    val selectedItems = useStateImpl {
        val latestKeyProvider = currentKeyProvider.value
        currentItems.value.filter { selectedKeys.value.contains(latestKeyProvider(it)) }
    }

    return remember {
        SelectableHolder(
            selectedItems = selectedItems,
            isSelected = { key -> selectedMap[key] == true },
            toggleSelected = { key ->
                val mode = currentSelectionMode.value
                Snapshot.withMutableSnapshot {
                    val nextSelected = !(selectedMap[key] ?: mode.getInitialSelected(key))
                    if (nextSelected && mode is SelectionMode.SingleSelect<*>) {
                        selectedMap.keys.toList().forEach { selectedMap[it] = it == key }
                    }
                    selectedMap[key] = nextSelected
                }
            },
            selectAll = {
                currentSelectionMode.value.requireMultiSelect()
                val latestKeyProvider = currentKeyProvider.value
                Snapshot.withMutableSnapshot {
                    currentItems.value.forEach { item -> selectedMap[latestKeyProvider(item)] = true }
                }
            },
            invertSelection = {
                currentSelectionMode.value.requireMultiSelect()
                val latestKeyProvider = currentKeyProvider.value
                Snapshot.withMutableSnapshot {
                    currentItems.value.forEach { item ->
                        val key = latestKeyProvider(item)
                        selectedMap[key] = !(selectedMap[key] ?: false)
                    }
                }
            },
            revertAll = {
                val latestKeyProvider = currentKeyProvider.value
                Snapshot.withMutableSnapshot {
                    currentItems.value.forEach { item -> selectedMap[latestKeyProvider(item)] = false }
                }
            },
        )
    }
}

@Composable
private fun <T> useIdentityState(value: T): State<T> {
    val state = remember { mutableStateOf(value, referentialEqualityPolicy()) }
    state.value = value
    return state
}

fun <KEY> SelectionMode<KEY>.requireMultiSelect() {
    if (this !is SelectionMode.MultiSelect) {
        throw IllegalStateException("Operation only allowed in multi-select mode")
    }
}

@Stable
data class SelectableHolder<KEY, ITEM>(
    val selectedItems: State<List<ITEM>>,
    val isSelected: IsSelected<KEY>,
    val toggleSelected: ToggleSelected<KEY>,
    val selectAll: SelectAction,
    val invertSelection: SelectAction,
    val revertAll: SelectAction,
)

typealias IsSelected<KEY> = (KEY) -> Boolean
typealias ToggleSelected<KEY> = (KEY) -> Unit
typealias SelectAction = () -> Unit
