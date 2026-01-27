@file:Suppress("UnusedReceiverParameter")

package xyz.junerver.compose.hooks.usetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.useContext

/*
  Description: Table hooks
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

/**
 * Object for scoping table-related hooks and functionality.
 * This object serves as a receiver for table hook extensions.
 */
object Table

/**
 * Creates a new table instance for managing table state.
 *
 * This hook provides a way to create and manage table state including sorting,
 * filtering, pagination, row selection, and column visibility.
 *
 * @return A new [TableInstance] for managing table state
 *
 * @example
 * ```kotlin
 * val table = Table.useTable<User>()
 *
 * Table(table, users, columns) {
 *     // Table content
 * }
 *
 * // Control from outside
 * Button(onClick = { table.toggleSorting("name") }) {
 *     Text("Sort by name")
 * }
 * ```
 */
@Composable
fun <T> Table.useTable(): TableInstance<T> = remember { TableInstance() }

/**
 * A hook for accessing the current table instance from child components.
 *
 * This hook provides access to the table instance from any component within the table's
 * context. It's particularly useful for:
 * - Creating reusable table components
 * - Accessing table state
 * - Implementing table controls
 *
 * @return The current [TableInstance]
 *
 * @example
 * ```kotlin
 * @Composable
 * fun TableControls() {
 *     val table = Table.useTableInstance<User>()
 *
 *     Row {
 *         Button(onClick = { table.clearSorting() }) {
 *             Text("Clear Sort")
 *         }
 *         Button(onClick = { table.clearFilters() }) {
 *             Text("Clear Filters")
 *         }
 *     }
 * }
 * ```
 */
@Suppress("UNCHECKED_CAST")
@Composable
fun <T> Table.useTableInstance(): TableInstance<T> = useContext(context = TableContext) as? TableInstance<T>
    ?: error("useTableInstance must be called within a Table component")
