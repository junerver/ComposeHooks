package xyz.junerver.compose.hooks

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testRef() {
        var out = ""
        val ref = MutableRef("init")
        ref.observe { out = it }
        assertEquals("init", out)
        ref.current = "hello"
        assertEquals("hello", out)
    }

    @Test
    fun testModifyList() {
        val list = mutableListOf<Double>()
        val timeM = measureTime {
            repeat(10_000) {
                list.add(Random.nextDouble())
            } // 8.6ms
        }
        println("mutable: $timeM")
        var listP = persistentListOf<Double>()
        val timeP = measureTime {
            repeat(10_000) {
                listP = listP.mutate {
                    it.add(Random.nextDouble())
                }
            } // 22.3ms
        }
        println("mutate: $timeP")
    }
}
