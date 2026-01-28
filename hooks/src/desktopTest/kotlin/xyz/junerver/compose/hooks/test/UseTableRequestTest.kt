package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
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
class UseTableRequestTest {
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
}
