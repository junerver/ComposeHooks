package xyz.junerver.compose.hooks

import org.junit.Assert.assertEquals
import org.junit.Test

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
}
