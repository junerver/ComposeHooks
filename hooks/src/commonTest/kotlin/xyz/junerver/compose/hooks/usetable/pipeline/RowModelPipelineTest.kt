package xyz.junerver.compose.hooks.usetable.pipeline

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usetable.core.Row
import xyz.junerver.compose.hooks.usetable.core.TableFeature
import xyz.junerver.compose.hooks.usetable.core.TableInstance
import xyz.junerver.compose.hooks.usetable.state.TableState
import kotlin.test.Test
import kotlin.test.assertEquals

class RowModelPipelineTest {

    data class TestData(val value: String)

    @Test
    fun `Pipeline should execute features by priority order`() {
        val executionOrder = mutableListOf<String>()
        
        // Feature with priority 200 (should run second)
        val sortFeature = object : TableFeature<TestData> {
            override val featureId = "sort"
            override val priority = 200
            
            @Composable
            override fun initState(instance: TableInstance<TestData>) {}
            
            override fun transform(
                rows: List<Row<TestData>>, 
                state: TableState<TestData>,
                columns: List<xyz.junerver.compose.hooks.usetable.core.ColumnDef<TestData, *>>
            ): List<Row<TestData>> {
                executionOrder.add("sort")
                return rows
            }
        }
        
        // Feature with priority 100 (should run first)
        val filterFeature = object : TableFeature<TestData> {
            override val featureId = "filter"
            override val priority = 100
            
            @Composable
            override fun initState(instance: TableInstance<TestData>) {}
            
            override fun transform(
                rows: List<Row<TestData>>, 
                state: TableState<TestData>,
                columns: List<xyz.junerver.compose.hooks.usetable.core.ColumnDef<TestData, *>>
            ): List<Row<TestData>> {
                executionOrder.add("filter")
                return rows
            }
        }
        
        val pipeline = RowModelPipeline(listOf(sortFeature, filterFeature))
        
        pipeline.execute(emptyList(), TableState(), emptyList())
        
        assertEquals(listOf("filter", "sort"), executionOrder)
    }
}
