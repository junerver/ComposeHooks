package xyz.junerver.composehooks.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.usetable.SortDirection
import xyz.junerver.compose.hooks.usetable.column
import xyz.junerver.compose.hooks.usetable.useTable

/*
  Description: useTable example
  Author: Claude
  Date: 2025/1/26
  Email: noreply@anthropic.com
  Version: v1.0
*/

private data class User(
    val id: Int,
    val name: String,
    val age: Int,
    val email: String,
    val department: String,
)

private val DEMO_USERS = listOf(
    User(1, "Alice", 28, "alice@example.com", "Engineering"),
    User(2, "Bob", 35, "bob@example.com", "Marketing"),
    User(3, "Charlie", 42, "charlie@example.com", "Engineering"),
    User(4, "Diana", 31, "diana@example.com", "HR"),
    User(5, "Eve", 26, "eve@example.com", "Engineering"),
    User(6, "Frank", 38, "frank@example.com", "Marketing"),
    User(7, "Grace", 29, "grace@example.com", "Finance"),
    User(8, "Henry", 45, "henry@example.com", "Engineering"),
    User(9, "Ivy", 33, "ivy@example.com", "HR"),
    User(10, "Jack", 27, "jack@example.com", "Finance"),
    User(11, "Kate", 36, "kate@example.com", "Marketing"),
    User(12, "Leo", 41, "leo@example.com", "Engineering"),
)

@Composable
fun UseTableExample() {
    val columns = listOf(
        column<User, Int>("id", header = "ID") { it.id },
        column<User, String>("name", header = "Name") { it.name },
        column<User, Int>("age", header = "Age") { it.age },
        column<User, String>("email", header = "Email") { it.email },
        column<User, String>("department", header = "Dept") { it.department },
    )

    val table = useTable(
        data = DEMO_USERS,
        columns = columns,
        optionsOf = {
            enableSorting = true
            enablePagination = true
            enableFiltering = true
            enableRowSelection = true
            pageSize = 5
            getRowId = { user, _ -> user.id.toString() }
        },
    )

    val rows by table.rows
    val sortingState by table.sortingState
    val paginationState by table.paginationState
    val filterState by table.filterState
    val canNextPage by table.canNextPage
    val canPreviousPage by table.canPreviousPage
    val pageCount by table.pageCount
    val selectedRows by table.selectedRows

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(
            text = "useTable Demo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Global filter
        OutlinedTextField(
            value = filterState.globalFilter,
            onValueChange = { table.setGlobalFilter(it) },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Selection info
        Text(
            text = "Selected: ${selectedRows.size} rows",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Table header
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
        ) {
            // Select all checkbox
            Box(modifier = Modifier.width(40.dp)) {
                Checkbox(
                    checked = selectedRows.size == rows.size && rows.isNotEmpty(),
                    onCheckedChange = { table.toggleAllRowsSelection() },
                )
            }

            columns.forEach { column ->
                val isSorted = sortingState.isSorted(column.id)
                val direction = sortingState.getDirection(column.id)

                Row(
                    modifier = Modifier.weight(1f).clickable { table.toggleSorting(column.id) }.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = column.header,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (isSorted) {
                        Icon(
                            imageVector = if (direction == SortDirection.ASC) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = "Sort direction",
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        // Table body
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rows, key = { it.id }) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(if (row.isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { table.toggleRowSelection(row.id) }
                        .padding(8.dp),
                ) {
                    Box(modifier = Modifier.width(40.dp)) {
                        Checkbox(
                            checked = row.isSelected,
                            onCheckedChange = { table.toggleRowSelection(row.id) },
                        )
                    }

                    Text(
                        text = row.original.id.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = row.original.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = row.original.age.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = row.original.email,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = row.original.department,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                HorizontalDivider()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pagination controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Page ${paginationState.pageIndex + 1} of $pageCount",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { table.previousPage() },
                    enabled = canPreviousPage,
                ) {
                    Text("Previous")
                }
                Button(
                    onClick = { table.nextPage() },
                    enabled = canNextPage,
                ) {
                    Text("Next")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { table.clearSorting() }) {
                Text("Clear Sort")
            }
            Button(onClick = { table.clearFilters() }) {
                Text("Clear Filter")
            }
            Button(onClick = { table.clearRowSelection() }) {
                Text("Clear Selection")
            }
        }
    }
}
