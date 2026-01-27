package xyz.junerver.composehooks.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.userequest.TableResult
import xyz.junerver.compose.hooks.userequest.useTableRequest
import xyz.junerver.compose.hooks.usetable.core.column
import xyz.junerver.compose.hooks.usetable.useTable
import kotlin.math.ceil

/**
 * Example demonstrating useTableRequest hook.
 *
 * Shows separation of concerns:
 * - useTableRequest: Handles data fetching and pagination state
 * - useTable: Handles local table features (sorting, filtering)
 */
@Composable
fun UseTableRequestExample() {
    // Mock data class
    data class User(val id: Int, val name: String, val age: Int)

    // Mock API request - returns TableResult directly
    suspend fun mockApiRequest(page: Int, pageSize: Int): TableResult<User> {
        delay(500) // Simulate network delay
        val allUsers = (1..25).map { User(it, "User$it", 20 + it) }
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allUsers.size)
        val rows = if (startIndex < allUsers.size) {
            allUsers.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        return TableResult(rows = rows, total = allUsers.size)
    }

    // 1. Use useTableRequest for data fetching
    val tableRequest = useTableRequest<User>(
        requestFn = { page, pageSize -> mockApiRequest(page, pageSize) },
        optionsOf = {
            initialPageSize = 5
            requestOptions = {
                onSuccess = { data, _ ->
                    println("Loaded ${data?.rows?.size ?: 0} users")
                }
                onError = { error, _ ->
                    println("Error: ${error.message}")
                }
            }
        }
    )

    // 2. Extract data for table
    val users by tableRequest.rows
    val total by tableRequest.total
    val loading by tableRequest.isLoading
    val currentPage by tableRequest.currentPage
    val pageSize by tableRequest.pageSize

    // 3. Define columns
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
            )
        )
    }

    // 4. Use useTable for local table features (sorting only)
    val table = useTable(
        data = users,
        columns = columns
    ) {
        enableSorting = true
    }

    val tableState by table.state
    val rowModel by table.rowModel
    val pageCount = remember(total, pageSize) {
        if (pageSize <= 0) 1 else ceil(total.toDouble() / pageSize).toInt()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title
        Text(
            text = "useTableRequest Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Controls Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Loading indicator or row count
            if (loading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Loading...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text(
                    "Showing ${rowModel.rows.size} of $total rows",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Refresh button
            IconButton(
                onClick = { tableRequest.refresh() },
                enabled = !loading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Table Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (sortDesc == true) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Sort",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // Table Body
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rowModel.rows.size) { index ->
                val row = rowModel.rows[index]

                Surface(
                    color = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.original.id.toString(),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = row.original.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = row.original.age.toString(),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pagination Controls
        var pageSizeExpanded by remember { mutableStateOf(false) }
        var jumpToPageText by remember { mutableStateOf("") }
        val pageSizeOptions = listOf(5, 10, 20)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First row: Page size selector and total info
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
                            Text("$pageSize")
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
                                        tableRequest.onPageChange(0, size)
                                        pageSizeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Total rows info
                Text("Total: $total rows", style = MaterialTheme.typography.bodyMedium)
            }

            // Second row: Navigation controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                Button(
                    onClick = { tableRequest.onPageChange(currentPage - 1, pageSize) },
                    enabled = currentPage > 0 && !loading
                ) {
                    Text("Previous")
                }

                // Page info and jump to page
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Page ${currentPage + 1} of $pageCount")

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
                            if (targetPage != null && targetPage in 1..pageCount) {
                                tableRequest.onPageChange(targetPage - 1, pageSize)
                                jumpToPageText = ""
                            }
                        },
                        enabled = jumpToPageText.toIntOrNull()?.let { it in 1..pageCount } == true && !loading
                    ) {
                        Text("Jump")
                    }
                }

                // Next button
                Button(
                    onClick = { tableRequest.onPageChange(currentPage + 1, pageSize) },
                    enabled = currentPage < pageCount - 1 && !loading
                ) {
                    Text("Next")
                }
            }
        }
    }
}
