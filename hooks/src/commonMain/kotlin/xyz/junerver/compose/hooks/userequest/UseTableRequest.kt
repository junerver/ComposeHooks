package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.userequest.utils.CachedData
import xyz.junerver.compose.hooks.utils.CacheManager
import xyz.junerver.compose.hooks.usetable.state.SortDescriptor

/**
 * Paged request parameters.
 *
 * @param page Page index (0-based)
 * @param pageSize Number of items per page
 */
data class TableRequestParams(
    val page: Int = 0,
    val pageSize: Int = 10,
    val sorting: List<SortDescriptor> = emptyList(),
    val globalFilter: String = "",
    val columnFilters: Map<String, Any?> = emptyMap()
)

private fun buildTableRequestCacheKey(baseKey: String, params: TableRequestParams): String {
    val sortingKey = params.sorting.joinToString("|") { sort ->
        "${sort.columnId}:${if (sort.desc) 1 else 0}"
    }
    val columnFiltersKey = params.columnFilters.entries
        .sortedBy { it.key }
        .joinToString("|") { (key, value) -> "$key=$value" }
    val filterHash = listOf(sortingKey, params.globalFilter, columnFiltersKey)
        .joinToString("#")
        .hashCode()
    return "${baseKey}_p${params.page}_s${params.pageSize}_f$filterHash"
}

/**
 * Standard table result container.
 *
 * This is the abstraction layer between your API response and the table hook.
 * Users map their API response format to this standard format in the requestFn closure.
 *
 * This is a pure data class - state management happens at the Holder level.
 *
 * @param rows Current page data (plain list, not State-wrapped)
 * @param total Total number of records
 */
data class TableResult<T>(
    val rows: List<T>,
    val total: Int
)

/**
 * Options for useTableRequest hook.
 */
class UseTableRequestOptions<TData : Any> {
    var initialSorting: List<SortDescriptor> = emptyList()
    var initialGlobalFilter: String = ""
    var initialColumnFilters: Map<String, Any?> = emptyMap()

    var triggerOnSortingChange: Boolean = true
    var triggerOnFilteringChange: Boolean = true

    /**
     * Initial page number (default: 0)
     */
    var initialPage: Int = 0

    /**
     * Initial page size (default: 10)
     */
    var initialPageSize: Int = 10

    /**
     * All useRequest options (cacheKey, staleTime, onSuccess, etc.)
     */
    var requestOptions: UseRequestOptions<TableRequestParams, TData>.() -> Unit = {}
    var mergeCacheKey: ((baseKey: String, params: TableRequestParams) -> String)? = null
    var setCache: ((data: CachedData<TData>) -> Unit)? = null
    var getCache: ((params: TableRequestParams) -> CachedData<TData>?)? = null
    var onRequestParams: ((TableRequestParams) -> Unit)? = null
}

/**
 * Return type for useTableRequest hook.
 *
 * State management happens at this Holder level, not in TableResult.
 */
data class TableRequestHolder<T>(
    // Current page data (State-wrapped at Holder level to avoid unnecessary recompositions)
    val rows: State<List<T>>,

    // Request states
    val isLoading: State<Boolean>,
    val error: State<Throwable?>,

    // Pagination states
    val currentPage: State<Int>,
    val pageSize: State<Int>,
    val total: State<Int>,

    // Sorting & filtering states
    val sorting: State<List<SortDescriptor>>,
    val globalFilter: State<String>,
    val columnFilters: State<Map<String, Any?>>,

    // Request controls
    val refresh: () -> Unit,
    val cancel: () -> Unit,

    // Pagination controls
    val onPageChange: (page: Int, pageSize: Int) -> Unit,

    // Sorting & filtering controls
    val setSorting: (List<SortDescriptor>) -> Unit,
    val setGlobalFilter: (String) -> Unit,
    val setColumnFilter: (String, Any?) -> Unit,
    val clearFilters: () -> Unit
)

/**
 * A hook for managing paginated table requests.
 *
 * This hook provides a clean abstraction for table pagination by using [TableResult] as
 * the standard container. Users map their API response format to TableResult in the
 * requestFn closure, and this hook handles all pagination state management.
 *
 * ## Key Design Principles:
 * 1. **Standard Container**: TableResult<T> abstracts away API response differences
 * 2. **Fixed Function Signature**: TableRequestParams -> TableResult<T>
 * 3. **User Does Mapping**: Convert API response to TableResult in requestFn
 * 4. **Avoid Unnecessary Recompositions**: rows wrapped in State
 *
 * ## Usage Example:
 * ```kotlin
 * // Your API response format
 * data class ApiResponse(val items: List<User>, val total: Int)
 *
 * // Use the hook - map API response to TableResult
 * val tableRequest = useTableRequest<User>(
 *     requestFn = { params ->
 *         val response = api.getUsers(params.page, params.pageSize)
 *         // Map your API format to TableResult (plain data class)
 *         TableResult(
 *             rows = response.items,
 *             total = response.total
 *         )
 *     },
 *     optionsOf = {
 *         initialPageSize = 20
 *         requestOptions = {
 *             cacheKey = "users"
 *             staleTime = 30.seconds
 *         }
 *     }
 * )
 *
 * // Access data through State-wrapped holder properties
 * val table = useTable(
 *     data = tableRequest.rows.value,  // rows is State<List<T>>
 *     columns = userColumns
 * ) {
 *     enableSorting = true
 * }
 *
 * // Pagination info
 * Pagination(
 *     total = tableRequest.total.value,
 *     current = tableRequest.currentPage.value,
 *     pageSize = tableRequest.pageSize.value,
 *     onChange = tableRequest.onPageChange
 * )
 * ```
 *
 * @param T Row data type
 * @param requestFn Async function: (params) -> TableResult<T>
 * @param optionsOf Configuration DSL
 */
@Composable
fun <T> useTableRequest(
    requestFn: suspend (params: TableRequestParams) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {}
): TableRequestHolder<T> {
    val options = UseTableRequestOptions<TableResult<T>>().apply(optionsOf)

    // 1. Pagination state
    val pageState = _useState(options.initialPage)
    val pageSizeState = _useState(options.initialPageSize)
    val currentPage = pageState.value
    val currentPageSize = pageSizeState.value

    // 2. Sorting & filtering state
    val sortingState = _useState(options.initialSorting)
    val globalFilterState = _useState(options.initialGlobalFilter)
    val columnFiltersState = _useState(options.initialColumnFilters)

    val currentSorting = sortingState.value
    val currentGlobalFilter = globalFilterState.value
    val currentColumnFilters = columnFiltersState.value

    val sortingDeps = if (options.triggerOnSortingChange) currentSorting else null
    val globalFilterDep = if (options.triggerOnFilteringChange) currentGlobalFilter else null
    val columnFiltersDep = if (options.triggerOnFilteringChange) currentColumnFilters else null

    val requestParamsState = _useState(
        TableRequestParams(
            page = currentPage,
            pageSize = currentPageSize,
            sorting = currentSorting,
            globalFilter = currentGlobalFilter,
            columnFilters = currentColumnFilters
        )
    )

    // 4. Use useRequest with manual mode (force manual to avoid duplicate auto-run)
    val requestHolder = useRequest<TableRequestParams, TableResult<T>>(
        requestFn = requestFn,
        optionsOf = {
            options.requestOptions(this)
            manual = true
            defaultParams = requestParamsState.value

            val customTableSetCache = options.setCache
            val customTableGetCache = options.getCache
            if (customTableSetCache != null) {
                setCache = { cachedData ->
                    customTableSetCache(cachedData)
                }
            }
            if (customTableGetCache != null) {
                getCache = { params ->
                    customTableGetCache(params)
                }
            }

            if (cacheKey.isNotEmpty()) {
                val baseCacheKey = cacheKey
                val mergeFullKey = options.mergeCacheKey
                val resolveKey: (TableRequestParams) -> String = { params ->
                    mergeFullKey?.invoke(baseCacheKey, params)
                        ?: buildTableRequestCacheKey(baseCacheKey, params)
                }
                val fallbackSetCache = setCache
                val fallbackGetCache = getCache
                setCache = { cachedData ->
                    val params = cachedData.params as? TableRequestParams
                    val key = if (params != null) {
                        resolveKey(params)
                    } else {
                        resolveKey(requestParamsState.value)
                    }
                    CacheManager.saveCache(key, cacheTime, cachedData)
                    fallbackSetCache?.invoke(cachedData)
                }
                getCache = { params ->
                    val key = resolveKey(params)
                    CacheManager.getCache<TableResult<T>>(key)
                        ?: fallbackGetCache?.invoke(params)
                }
            }
        }
    )

    val refresh: () -> Unit = {
        requestHolder.request(requestParamsState.value)
    }

    // 5. Auto-fetch when pagination/sorting/filtering changes
    useEffect(currentPage, currentPageSize, sortingDeps, globalFilterDep, columnFiltersDep) {
        val newParams = TableRequestParams(
            page = currentPage,
            pageSize = currentPageSize,
            sorting = currentSorting,
            globalFilter = currentGlobalFilter,
            columnFilters = currentColumnFilters
        )
        requestParamsState.value = newParams
        options.onRequestParams?.invoke(newParams)
        requestHolder.request(newParams)
    }

    // 6. Extract rows and total from TableResult (now plain data, wrap in State at Holder level)
    val rows = remember(requestHolder.data.value) {
        derivedStateOf {
            requestHolder.data.value?.rows ?: emptyList()
        }
    }

    val total = remember(requestHolder.data.value) {
        derivedStateOf {
            requestHolder.data.value?.total ?: 0
        }
    }

    // 7. Pagination controls
    val onPageChange: (Int, Int) -> Unit = { newPage, newSize ->
        if (newSize != currentPageSize) {
            pageSizeState.value = newSize
            pageState.value = 0  // Reset to first page
        } else if (newPage != currentPage) {
            pageState.value = newPage
        }
    }

    // 8. Sorting & filtering controls
    val setSorting: (List<SortDescriptor>) -> Unit = { sorting ->
        sortingState.value = sorting
    }

    val setGlobalFilter: (String) -> Unit = { filter ->
        globalFilterState.value = filter
    }

    val setColumnFilter: (String, Any?) -> Unit = { columnId, value ->
        columnFiltersState.value = if (value == null) {
            columnFiltersState.value - columnId
        } else {
            columnFiltersState.value + (columnId to value)
        }
    }

    val clearFilters: () -> Unit = {
        columnFiltersState.value = emptyMap()
        globalFilterState.value = ""
    }

    // 9. Return holder
    return TableRequestHolder(
        rows = rows,
        isLoading = requestHolder.isLoading,
        error = requestHolder.error,
        currentPage = pageState,
        pageSize = pageSizeState,
        total = total,
        sorting = sortingState,
        globalFilter = globalFilterState,
        columnFilters = columnFiltersState,
        refresh = refresh,
        cancel = requestHolder.cancel,
        onPageChange = onPageChange,
        setSorting = setSorting,
        setGlobalFilter = setGlobalFilter,
        setColumnFilter = setColumnFilter,
        clearFilters = clearFilters
    )
}

/**
 * Alias for [useTableRequest] following Compose naming convention.
 */
@Composable
fun <T> rememberTableRequest(
    requestFn: suspend (params: TableRequestParams) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {}
): TableRequestHolder<T> = useTableRequest(requestFn, optionsOf)

@Composable
fun <T> useTableRequest(
    requestFn: suspend (page: Int, pageSize: Int) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {}
): TableRequestHolder<T> = useTableRequest(
    requestFn = { params: TableRequestParams -> requestFn(params.page, params.pageSize) },
    optionsOf = optionsOf
)

@Composable
fun <T> rememberTableRequest(
    requestFn: suspend (page: Int, pageSize: Int) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {}
): TableRequestHolder<T> = useTableRequest(requestFn, optionsOf)
