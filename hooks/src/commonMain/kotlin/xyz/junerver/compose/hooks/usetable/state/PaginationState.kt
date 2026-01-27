package xyz.junerver.compose.hooks.usetable.state

import androidx.compose.runtime.Stable

@Stable
data class PaginationState(
    val pageIndex: Int = 0,
    val pageSize: Int = 10
)
