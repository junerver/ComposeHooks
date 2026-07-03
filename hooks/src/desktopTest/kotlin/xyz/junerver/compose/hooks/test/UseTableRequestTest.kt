package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.userequest.TableRequestParams
import xyz.junerver.compose.hooks.userequest.TableResult
import xyz.junerver.compose.hooks.userequest.useTableRequest
import xyz.junerver.compose.hooks.usetable.state.SortDescriptor

/*
  Description: useTableRequest 组合行为桌面测试
  Author: Junerver
  Date: 2026/1/15
  Email: junerver@gmail.com
  Version: v1.0
*/
@Suppress("DEPRECATION")
class UseTableRequestTest {
    private fun waitForCondition(maxAttempts: Int = 80, delayMs: Long = 20, condition: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun requestOptions_manual_false_does_not_double_request() = runComposeUiTest {
        val callCount = AtomicInteger(0)
        setContent {
            useTableRequest<Int>(
                requestFn = { _: TableRequestParams ->
                    callCount.incrementAndGet()
                    TableResult(emptyList(), 0)
                },
                optionsOf = {
                    requestOptions = {
                        manual = false
                    }
                },
            )
            Text("count=${callCount.get()}")
        }

        waitForIdle()
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(1, callCount.get())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun sorting_and_filter_changes_trigger_request() = runComposeUiTest {
        val paramsHistory = mutableListOf<TableRequestParams>()
        setContent {
            val holder = useTableRequest<String>(
                requestFn = { params ->
                    paramsHistory.add(params)
                    TableResult(emptyList(), 0)
                },
            )

            SideEffect {
                if (paramsHistory.size == 1) {
                    holder.setSorting(listOf(SortDescriptor("name", desc = true)))
                    holder.setGlobalFilter("keyword")
                    holder.setColumnFilter("status", "active")
                }
            }

            Text("count=${paramsHistory.size}")
        }

        waitForIdle()
        mainClock.advanceTimeBy(200)
        waitForIdle()

        assertTrue(paramsHistory.size >= 2)
        val last = paramsHistory.last()
        assertEquals(listOf(SortDescriptor("name", desc = true)), last.sorting)
        assertEquals("keyword", last.globalFilter)
        assertEquals("active", last.columnFilters["status"])
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pageSize_change_resets_page_to_zero_and_requests_new_params() = runComposeUiTest {
        val paramsHistory = mutableListOf<TableRequestParams>()
        lateinit var latestPage: () -> Int
        lateinit var latestPageSize: () -> Int

        setContent {
            val holder = useTableRequest<String>(
                requestFn = { params ->
                    paramsHistory.add(params)
                    TableResult(emptyList(), 50)
                },
                optionsOf = {
                    initialPage = 2
                    initialPageSize = 10
                },
            )
            latestPage = { holder.currentPage.value }
            latestPageSize = { holder.pageSize.value }

            SideEffect {
                if (paramsHistory.size == 1) {
                    holder.onPageChange(2, 20)
                }
            }

            Text("count=${paramsHistory.size}")
        }

        waitForIdle()
        val updated = waitForCondition {
            paramsHistory.size >= 2 &&
                paramsHistory.last().page == 0 &&
                paramsHistory.last().pageSize == 20
        }
        assertTrue(updated, "Expected pageSize change to reset page and trigger a new request")
        assertEquals(0, latestPage())
        assertEquals(20, latestPageSize())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clearFilters_restores_empty_filters_and_triggers_request() = runComposeUiTest {
        val paramsHistory = mutableListOf<TableRequestParams>()
        lateinit var latestGlobalFilter: () -> String
        lateinit var latestColumnFilters: () -> Map<String, Any?>

        setContent {
            var phase by useState(default = 0)
            val holder = useTableRequest<String>(
                requestFn = { params ->
                    paramsHistory.add(params)
                    TableResult(emptyList(), 0)
                },
            )
            latestGlobalFilter = { holder.globalFilter.value }
            latestColumnFilters = { holder.columnFilters.value }

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.setGlobalFilter("keyword")
                        holder.setColumnFilter("status", "active")
                        phase = 1
                    }

                    1 -> {
                        if (holder.globalFilter.value == "keyword" && holder.columnFilters.value["status"] == "active") {
                            holder.clearFilters()
                            phase = 2
                        }
                    }
                }
            }

            Text("phase=$phase count=${paramsHistory.size}")
        }

        waitForIdle()
        val cleared = waitForCondition {
            paramsHistory.any { it.globalFilter == "keyword" && it.columnFilters["status"] == "active" } &&
                paramsHistory.last().globalFilter.isEmpty() &&
                paramsHistory.last().columnFilters.isEmpty()
        }
        assertTrue(cleared, "Expected clearFilters to reset filter state and request with empty filters")
        assertEquals("", latestGlobalFilter())
        assertTrue(latestColumnFilters().isEmpty())
    }
}
