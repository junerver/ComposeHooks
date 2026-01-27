package xyz.junerver.composehooks.example

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.usetable.Table
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

    // Unpack table state for custom controls
    val tableState by table.state
    val rowModel by table.rowModel
    val globalFilter = tableState.filtering.globalFilter
    val selectedCount = tableState.rowSelection.selectedRowIds.size

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

        // Use the Headless Table Component
        Table(table = table) {

            // 1. Header (Custom UI)
            TableHeader { columns, state ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        // Select All
                        Box(modifier = Modifier.width(40.dp)) {
                            Checkbox(
                                checked = selectedCount == rowModel.rows.size && rowModel.rows.isNotEmpty(),
                                onCheckedChange = { table.toggleAllRowsSelection(it) }
                            )
                        }

                        // Columns
                        columns.forEach { column ->
                            val isSorted = state.sorting.sorting.any { it.columnId == column.id }
                            val sortDesc = state.sorting.sorting.find { it.columnId == column.id }?.desc

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
            }

            HorizontalDivider()

            // 2. Body (Custom UI Logic)
            TableBody { rows ->
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(rows.size) { index ->
                        val row = rows[index]
                        val isSelected = tableState.rowSelection.selectedRowIds.contains(row.id)

                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().clickable { table.toggleRowSelection(row.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Pagination (Enhanced UI with page size selector and jump to page)
            TablePagination { pagination ->
                var pageSizeExpanded by remember { mutableStateOf(false) }
                var jumpToPageText by remember { mutableStateOf("") }
                val pageSizeOptions = listOf(5, 10, 20)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // First row: Page size selector and page info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Page size selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rows per page:", style = MaterialTheme.typography.bodyMedium)
                            
                            Box {
                                OutlinedButton(
                                    onClick = { pageSizeExpanded = true },
                                    modifier = Modifier.width(80.dp)
                                ) {
                                    Text("${pagination.pageSize}")
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select page size",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = pageSizeExpanded,
                                    onDismissRequest = { pageSizeExpanded = false }
                                ) {
                                    pageSizeOptions.forEach { size ->
                                        DropdownMenuItem(
                                            text = { Text("$size") },
                                            onClick = {
                                                pagination.setPageSize(size)
                                                pageSizeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Total rows info
                        Text(
                            "Total: ${pagination.totalRows} rows",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Second row: Navigation controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous button
                        Button(
                            onClick = pagination.previousPage,
                            enabled = pagination.canPrev
                        ) {
                            Text("Previous")
                        }

                        // Page info and jump to page
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Page ${pagination.pageIndex + 1} of ${pagination.pageCount}")
                            
                            Text("|", color = MaterialTheme.colorScheme.outline)
                            
                            Text("Go to:", style = MaterialTheme.typography.bodyMedium)
                            
                            OutlinedTextField(
                                value = jumpToPageText,
                                onValueChange = { jumpToPageText = it.filter { char -> char.isDigit() } },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            
                            Button(
                                onClick = {
                                    val targetPage = jumpToPageText.toIntOrNull()
                                    if (targetPage != null && targetPage in 1..pagination.pageCount) {
                                        pagination.setPageIndex(targetPage - 1)
                                        jumpToPageText = ""
                                    }
                                },
                                enabled = jumpToPageText.toIntOrNull()?.let { 
                                    it in 1..pagination.pageCount 
                                } == true
                            ) {
                                Text("Jump")
                            }
                        }

                        // Next button
                        Button(
                            onClick = pagination.nextPage,
                            enabled = pagination.canNext
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}

