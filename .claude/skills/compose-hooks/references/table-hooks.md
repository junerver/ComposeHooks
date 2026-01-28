# Table Hooks

## useTable

无头表格，负责行模型与状态，不负责 UI 渲染。

```kotlin
val table = useTable(
    data = users,
    columns = listOf(
        column<User, String>("name") { it.name },
        column<User, Int>("age") { it.age },
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
val tableRequest = useTableRequest<User>(
    requestFn = { page, pageSize -> api.fetch(page, pageSize) },
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
- 翻页时把 `tableRequest.pageIndex` / `pageSize` 传入请求

## remember 别名

- `rememberTable` 目前没有专用别名，可直接使用 `useTable`
- `rememberTableRequest` 目前没有专用别名，可直接使用 `useTableRequest`

## 依据

- useTable: hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usetable/hooks.kt
- Table/Scope: hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usetable/Table.kt
- useTableRequest: hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/userequest/UseTableRequest.kt
