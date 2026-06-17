# Table Hooks

## useTable

无头表格，负责行模型与状态，不负责 UI 渲染。`rememberTable` 是同签名别名。

```kotlin
val table = useTable(
    data = users,
    columns = listOf(
        column<User, String>("name", header = "姓名") { it.name },
        column<User, Int>("age", header = "年龄") { it.age },
    ),
) {
    enableSorting = true
    enableFiltering = true
    enablePagination = true
    pageSize = 10
}
```

- 行模型：`table.rowModel.value.rows`
- 表格状态：`table.state.value`
- 排序/过滤/分页：使用 `table.toggleSorting`、`table.setGlobalFilter`、`table.setPageIndex` 等

## Table 组合

```kotlin
Table(table) {
    TableHeader { columns, state ->
        // 渲染表头
    }

    TableBody { rows ->
        // 渲染行
    }

    TablePagination { page ->
        // page.pageIndex / page.pageCount / page.nextPage()
    }
}
```

## useTableRequest

分页请求 + 表格组合用法：

```kotlin
val tableRequest = useTableRequest(
    requestFn = { params: TableRequestParams ->
        val response = api.fetch(params.page, params.pageSize)
        TableResult(rows = response.items, total = response.total)
    },
    optionsOf = { initialPageSize = 20 },
)

val table = useTable(
    data = tableRequest.rows.value,
    columns = columns,
) {
    enablePagination = true
}
```

- `tableRequest.rows` / `tableRequest.total` 为 `State`
- 翻页使用 `tableRequest.onPageChange(page, pageSize)`，当前页和页大小分别在 `currentPage` / `pageSize`
- 兼容重载也支持 `requestFn = { page, pageSize -> TableResult(...) }`

## remember 别名

- `rememberTable(data, columns) { ... }` 等价于 `useTable(data, columns) { ... }`
- `rememberTableRequest(...)` 等价于 `useTableRequest(...)`
- `Table.useTable()` / `Table.useTableInstance()` 以及对应 `remember` 版本是废弃入口，不要在新代码中使用

## 依据

- useTable: hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usetable/hooks.kt
- Table/Scope: hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usetable/Table.kt
- useTableRequest: hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/userequest/UseTableRequest.kt
