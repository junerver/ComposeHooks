package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
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
fun <KEY, ITEM> useSelectable(
    selectionMode: SelectionMode<KEY>,
    items: List<ITEM>,
    keyProvider: (ITEM) -> KEY,
): SelectableHolder<KEY, ITEM> {
    val initialMap = useCreation {
        items.map { keyProvider(it) to selectionMode.getInitialSelected(keyProvider(it)) }
    }
    val selectedMap = useMap(pairs = initialMap.current)

    val selectedKeys = useState { selectedMap.filterValues { it }.keys }
    val selectedItems = useState {
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
            revertAll = revertAll
        )
    }
}

fun <KEY> SelectionMode<KEY>.requireMultiSelect() {
    if (this !is SelectionMode.MultiSelect) {
        throw IllegalStateException("Operation only allowed in multi-select mode")
    }
}

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

sealed class SelectionMode<KEY> {
    class SingleSelect<KEY>(private val defaultSelectKey: KEY? = null) : SelectionMode<KEY>() {
        override fun getInitialSelected(key: KEY): Boolean = defaultSelectKey == key
    }

    class MultiSelect<KEY>(private val defaultSelectKeys: Set<KEY>? = null) : SelectionMode<KEY>() {
        override fun getInitialSelected(key: KEY): Boolean = defaultSelectKeys?.contains(key) ?: false
    }

    abstract fun getInitialSelected(key: KEY): Boolean
}
