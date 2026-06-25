package xyz.junerver.compose.hooks.useselectable
import xyz.junerver.compose.hooks.usestate.useStateImpl
import xyz.junerver.compose.hooks.usemap.useMapImpl
import xyz.junerver.compose.hooks.usecreation.useCreationImpl
import xyz.junerver.compose.hooks.SelectionMode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

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
    val initialMap = useCreationImpl {
        items.map { keyProvider(it) to selectionMode.getInitialSelected(keyProvider(it)) }
    }
    val selectedMap = useMapImpl(pairs = initialMap.current)

    val selectedKeys = useStateImpl { selectedMap.filterValues { it }.keys }
    val selectedItems = useStateImpl {
        items.filter { selectedKeys.value.contains(keyProvider(it)) }
    }

    val isSelected = { key: KEY -> selectedKeys.value.contains(key) }

    val toggleSelected = { key: KEY ->
        selectedMap[key] = !(selectedMap[key] ?: selectionMode.getInitialSelected(key))
        if (selectionMode is SelectionMode.SingleSelect<*>) {
            selectedKeys.value.forEach {
                if (it != key) selectedMap[it] = false
            }
        }
    }

    val selectAll = {
        selectionMode.requireMultiSelect()
        items.forEach { item ->
            selectedMap[keyProvider(item)] = true
        }
    }

    val revertAll = {
        items.forEach { item ->
            selectedMap[keyProvider(item)] = false
        }
    }

    val invertSelection = {
        selectionMode.requireMultiSelect()
        items.forEach { item ->
            val key = keyProvider(item)
            selectedMap[key] = !(selectedMap[key] ?: false)
        }
    }

    return remember {
        SelectableHolder(
            selectedItems = selectedItems,
            isSelected = isSelected,
            toggleSelected = toggleSelected,
            selectAll = selectAll,
            invertSelection = invertSelection,
            revertAll = revertAll,
        )
    }
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
