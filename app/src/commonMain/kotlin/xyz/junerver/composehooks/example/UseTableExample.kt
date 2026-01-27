package xyz.junerver.composehooks.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.useTable

private data class User(
    val id: Int,
    val name: String,
    val age: Int,
    val email: String,
    val department: String,
)

@Composable
fun UseTableExample() {
    val users = remember {
        listOf(
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
    }

    val columns = remember {
        listOf(
            column<User, Int>(
                id = "id",
                header = "ID",
                accessorFn = { it.id }
            ),
            column<User, String>(
                id = "name",
                header = "Name",
                accessorFn = { it.name }
            ),
            column<User, Int>(
                id = "age",
                header = "Age",
                accessorFn = { it.age }
            ),
            column<User, String>(
                id = "email",
                header = "Email",
                accessorFn = { it.email }
            ),
            column<User, String>(
                id = "department",
                header = "Dept",
                accessorFn = { it.department }
            )
        )
    }

    // Use the headless table hook
    val table = useTable(
        data = users,
        columns = columns
    ) {
        enableSorting = true
        enableFiltering = true
        enablePagination = true
        enableRowSelection = true
        pageSize = 5
        getRowId = { user, _ -> user.id.toString() }
    }

    // Destructure state for easier access
    val rowModel by table.rowModel
    val tableState by table.state
    
    // Derived UI state
    val globalFilter = tableState.filtering.globalFilter
    val selectedCount = tableState.rowSelection.selectedRowIds.size
    val pageIndex = tableState.pagination.pageIndex
    val pageSize = tableState.pagination.pageSize
    
    // Calculate pagination state manually
    val totalRows = users.size
    val pageCount = if (pageSize <= 0) 1 else kotlin.math.ceil(totalRows.toDouble() / pageSize).toInt()
    val canNext = pageIndex < pageCount - 1
    val canPrev = pageIndex > 0

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "useTable Demo (Headless)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Controls Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = globalFilter,
                onValueChange = { table.setGlobalFilter(it) },
                label = { Text("Search") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            Button(onClick = { table.clearFilters() }) {
                Text("Clear")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Selected: $selectedCount rows")

        Spacer(modifier = Modifier.height(8.dp))

        // Table Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                // Select All Checkbox
                Box(modifier = Modifier.width(40.dp)) {
                    Checkbox(
                        checked = selectedCount == rowModel.rows.size && rowModel.rows.isNotEmpty(),
                        onCheckedChange = { table.toggleAllRowsSelection(it) }
                    )
                }
                
                // Column Headers
                columns.forEach { column ->
                    val isSorted = tableState.sorting.sorting.any { it.columnId == column.id }
                    val sortDesc = tableState.sorting.sorting.find { it.columnId == column.id }?.desc
                    
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { table.toggleSorting(column.id, null) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = column.header,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSorted) {
                            Icon(
                                imageVector = if (sortDesc == true) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Sort"
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // Table Rows
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rowModel.rows.size) { index ->
                val row = rowModel.rows[index]
                val isSelected = tableState.rowSelection.selectedRowIds.contains(row.id)
                
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().clickable { table.toggleRowSelection(row.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Selection Checkbox
                        Box(modifier = Modifier.width(40.dp)) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { table.toggleRowSelection(row.id) }
                            )
                        }
                        
                        // Cells
                        Text(text = row.original.id.toString(), modifier = Modifier.weight(1f))
                        Text(text = row.original.name, modifier = Modifier.weight(1f))
                        Text(text = row.original.age.toString(), modifier = Modifier.weight(1f))
                        Text(text = row.original.email, modifier = Modifier.weight(1f))
                        Text(text = row.original.department, modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pagination Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { table.previousPage() },
                enabled = canPrev
            ) {
                Text("Previous")
            }

            Text("Page ${pageIndex + 1} of $pageCount")

            Button(
                onClick = { table.nextPage() },
                enabled = canNext
            ) {
                Text("Next")
            }
        }
    }
}
