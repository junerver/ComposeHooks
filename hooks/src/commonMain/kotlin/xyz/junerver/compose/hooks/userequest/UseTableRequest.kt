package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useLatestRef

/**
 * Paged request parameters.
 *
 * @param page Page index (0-based)
 * @param pageSize Number of items per page
 */
data class PageParams(
    val page: Int = 0,
    val pageSize: Int = 10
)

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
    var requestOptions: UseRequestOptions<PageParams, TData>.() -> Unit = {}
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

    // Request controls
    val refresh: () -> Unit,
    val cancel: () -> Unit,

    // Pagination controls
    val onPageChange: (page: Int, pageSize: Int) -> Unit
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
 * 2. **Fixed Function Signature**: (page, pageSize) -> TableResult<T>
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
 *     requestFn = { page, pageSize ->
 *         val response = api.getUsers(page, pageSize)
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
 * @param requestFn Async function: (page, pageSize) -> TableResult<T>
 * @param optionsOf Configuration DSL
 */
@Composable
fun <T> useTableRequest(
    requestFn: suspend (page: Int, pageSize: Int) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {}
): TableRequestHolder<T> {
    val options = UseTableRequestOptions<TableResult<T>>().apply(optionsOf)

    // 1. Pagination state
    val pageState = _useState(options.initialPage)
    val pageSizeState = _useState(options.initialPageSize)
    val currentPage = pageState.value
    val currentPageSize = pageSizeState.value

    // 2. Use refs for latest pagination values
    val latestPage = useLatestRef(currentPage)
    val latestPageSize = useLatestRef(currentPageSize)

    // 3. Use useRequest with manual mode
    val requestHolder = useRequest<PageParams, TableResult<T>>(
        requestFn = { params -> requestFn(params.page, params.pageSize) },
        optionsOf = {
            manual = true
            defaultParams = PageParams(latestPage.current, latestPageSize.current)
            options.requestOptions(this)
        }
    )

    // 4. Auto-fetch when pagination changes
    useEffect(currentPage, currentPageSize) {
        val pageParams = PageParams(latestPage.current, latestPageSize.current)
        requestHolder.request(pageParams)
    }

    // 5. Extract rows and total from TableResult (now plain data, wrap in State at Holder level)
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

    // 6. Pagination controls
    val onPageChange: (Int, Int) -> Unit = { newPage, newSize ->
        if (newSize != currentPageSize) {
            pageSizeState.value = newSize
            pageState.value = 0  // Reset to first page
        } else if (newPage != currentPage) {
            pageState.value = newPage
        }
    }

    // 7. Return holder
    return TableRequestHolder(
        rows = rows,
        isLoading = requestHolder.isLoading,
        error = requestHolder.error,
        refresh = requestHolder.refresh,
        cancel = requestHolder.cancel,
        currentPage = pageState,
        pageSize = pageSizeState,
        total = total,
        onPageChange = onPageChange
    )
}

/**
 * Alias for [useTableRequest] following Compose naming convention.
 */
@Composable
fun <T> rememberTableRequest(
    requestFn: suspend (page: Int, pageSize: Int) -> TableResult<T>,
    optionsOf: UseTableRequestOptions<TableResult<T>>.() -> Unit = {}
): TableRequestHolder<T> = useTableRequest(requestFn, optionsOf)
