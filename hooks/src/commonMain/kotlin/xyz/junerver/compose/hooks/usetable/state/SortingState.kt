package xyz.junerver.compose.hooks.usetable.state

import androidx.compose.runtime.Stable

/**
 * Descriptor for a single column sort.
 */
@Stable
data class SortDescriptor(
    val columnId: String,
    val desc: Boolean = false
)

/**
 * State for sorting feature.
 */
@Stable
data class SortingState(
    val sorting: List<SortDescriptor> = emptyList()
)
