package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.math.truncate
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.usemath.useAbsImpl
import xyz.junerver.compose.hooks.usemath.useCeilImpl
import xyz.junerver.compose.hooks.usemath.useFloorImpl
import xyz.junerver.compose.hooks.usemath.useMaxImpl
import xyz.junerver.compose.hooks.usemath.useMinImpl
import xyz.junerver.compose.hooks.usemath.usePowImpl
import xyz.junerver.compose.hooks.usemath.useRoundImpl
import xyz.junerver.compose.hooks.usemath.useSqrtImpl
import xyz.junerver.compose.hooks.usemath.useTruncImpl

/*
  Description: Comprehensive tests for usemath hooks
  Author: MiMoCode
  Date: 2026/6/26
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseMathTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseAbsImplInt() = runComposeUiTest {
        var result1: State<Int>? = null
        var result2: State<Int>? = null
        var result3: State<Int>? = null
        setContent {
            result1 = useAbsImpl(-5)
            result2 = useAbsImpl(5)
            result3 = useAbsImpl(0)
        }
        waitForIdle()
        assertEquals(abs(-5), result1!!.value)
        assertEquals(abs(5), result2!!.value)
        assertEquals(abs(0), result3!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseAbsImplDouble() = runComposeUiTest {
        var result1: State<Double>? = null
        var result2: State<Double>? = null
        var result3: State<Double>? = null
        setContent {
            result1 = useAbsImpl(-3.14)
            result2 = useAbsImpl(2.71)
            result3 = useAbsImpl(0.0)
        }
        waitForIdle()
        assertEquals(abs(-3.14), result1!!.value)
        assertEquals(abs(2.71), result2!!.value)
        assertEquals(abs(0.0), result3!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseAbsImplFloat() = runComposeUiTest {
        var result1: State<Float>? = null
        var result2: State<Float>? = null
        setContent {
            result1 = useAbsImpl(-1.5f)
            result2 = useAbsImpl(2.5f)
        }
        waitForIdle()
        assertEquals(abs(-1.5f), result1!!.value)
        assertEquals(abs(2.5f), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseAbsImplLong() = runComposeUiTest {
        var result1: State<Long>? = null
        var result2: State<Long>? = null
        setContent {
            result1 = useAbsImpl(-100L)
            result2 = useAbsImpl(100L)
        }
        waitForIdle()
        assertEquals(abs(-100L), result1!!.value)
        assertEquals(abs(100L), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseCeilImplDouble() = runComposeUiTest {
        var result1: State<Double>? = null
        var result2: State<Double>? = null
        var result3: State<Double>? = null
        setContent {
            result1 = useCeilImpl(2.1)
            result2 = useCeilImpl(-2.1)
            result3 = useCeilImpl(3.0)
        }
        waitForIdle()
        assertEquals(ceil(2.1), result1!!.value)
        assertEquals(ceil(-2.1), result2!!.value)
        assertEquals(ceil(3.0), result3!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseCeilImplFloat() = runComposeUiTest {
        var result1: State<Float>? = null
        var result2: State<Float>? = null
        setContent {
            result1 = useCeilImpl(2.1f)
            result2 = useCeilImpl(-2.1f)
        }
        waitForIdle()
        assertEquals(ceil(2.1f), result1!!.value)
        assertEquals(ceil(-2.1f), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseCeilImplInt() = runComposeUiTest {
        var result: State<Int>? = null
        setContent {
            result = useCeilImpl(5)
        }
        waitForIdle()
        assertEquals(5, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseCeilImplLong() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useCeilImpl(10L)
        }
        waitForIdle()
        assertEquals(10L, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseFloorImplDouble() = runComposeUiTest {
        var result1: State<Double>? = null
        var result2: State<Double>? = null
        var result3: State<Double>? = null
        setContent {
            result1 = useFloorImpl(2.9)
            result2 = useFloorImpl(-2.1)
            result3 = useFloorImpl(3.0)
        }
        waitForIdle()
        assertEquals(floor(2.9), result1!!.value)
        assertEquals(floor(-2.1), result2!!.value)
        assertEquals(floor(3.0), result3!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseFloorImplFloat() = runComposeUiTest {
        var result1: State<Float>? = null
        var result2: State<Float>? = null
        setContent {
            result1 = useFloorImpl(2.9f)
            result2 = useFloorImpl(-2.1f)
        }
        waitForIdle()
        assertEquals(floor(2.9f), result1!!.value)
        assertEquals(floor(-2.1f), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseFloorImplInt() = runComposeUiTest {
        var result: State<Int>? = null
        setContent {
            result = useFloorImpl(5)
        }
        waitForIdle()
        assertEquals(5, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseFloorImplLong() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useFloorImpl(10L)
        }
        waitForIdle()
        assertEquals(10L, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseRoundImplDouble() = runComposeUiTest {
        var result1: State<Double>? = null
        var result2: State<Double>? = null
        var result3: State<Double>? = null
        var result4: State<Double>? = null
        setContent {
            result1 = useRoundImpl(2.3)
            result2 = useRoundImpl(2.5)
            result3 = useRoundImpl(2.7)
            result4 = useRoundImpl(-2.5)
        }
        waitForIdle()
        assertEquals(round(2.3), result1!!.value)
        assertEquals(round(2.5), result2!!.value)
        assertEquals(round(2.7), result3!!.value)
        assertEquals(round(-2.5), result4!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseRoundImplFloat() = runComposeUiTest {
        var result1: State<Float>? = null
        var result2: State<Float>? = null
        setContent {
            result1 = useRoundImpl(2.5f)
            result2 = useRoundImpl(-2.5f)
        }
        waitForIdle()
        assertEquals(round(2.5f), result1!!.value)
        assertEquals(round(-2.5f), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseRoundImplInt() = runComposeUiTest {
        var result: State<Int>? = null
        setContent {
            result = useRoundImpl(5)
        }
        waitForIdle()
        assertEquals(5, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseRoundImplLong() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useRoundImpl(10L)
        }
        waitForIdle()
        assertEquals(10L, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseTruncImplDouble() = runComposeUiTest {
        var result1: State<Double>? = null
        var result2: State<Double>? = null
        var result3: State<Double>? = null
        setContent {
            result1 = useTruncImpl(2.9)
            result2 = useTruncImpl(-2.9)
            result3 = useTruncImpl(3.0)
        }
        waitForIdle()
        assertEquals(truncate(2.9), result1!!.value)
        assertEquals(truncate(-2.9), result2!!.value)
        assertEquals(truncate(3.0), result3!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseTruncImplFloat() = runComposeUiTest {
        var result1: State<Float>? = null
        var result2: State<Float>? = null
        setContent {
            result1 = useTruncImpl(2.9f)
            result2 = useTruncImpl(-2.9f)
        }
        waitForIdle()
        assertEquals(truncate(2.9f), result1!!.value)
        assertEquals(truncate(-2.9f), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseTruncImplInt() = runComposeUiTest {
        var result: State<Int>? = null
        setContent {
            result = useTruncImpl(5)
        }
        waitForIdle()
        assertEquals(5, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseTruncImplLong() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useTruncImpl(10L)
        }
        waitForIdle()
        assertEquals(10L, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplIntInt() = runComposeUiTest {
        var result1: State<Int>? = null
        var result2: State<Int>? = null
        setContent {
            result1 = useMinImpl(3, 5)
            result2 = useMinImpl(5, 3)
        }
        waitForIdle()
        assertEquals(min(3, 5), result1!!.value)
        assertEquals(min(5, 3), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplLongLong() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useMinImpl(3L, 5L)
        }
        waitForIdle()
        assertEquals(min(3L, 5L), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplFloatFloat() = runComposeUiTest {
        var result: State<Float>? = null
        setContent {
            result = useMinImpl(3.5f, 5.5f)
        }
        waitForIdle()
        assertEquals(min(3.5f, 5.5f), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplDoubleDouble() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useMinImpl(3.5, 5.5)
        }
        waitForIdle()
        assertEquals(min(3.5, 5.5), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplIntLong() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useMinImpl(3, 5L)
        }
        waitForIdle()
        assertEquals(min(3L, 5L), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplLongInt() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useMinImpl(3L, 5)
        }
        waitForIdle()
        assertEquals(min(3L, 5L), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplFloatDouble() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useMinImpl(3.5f, 5.5)
        }
        waitForIdle()
        assertEquals(min(3.5, 5.5), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplDoubleFloat() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useMinImpl(3.5, 5.5f)
        }
        waitForIdle()
        assertEquals(min(3.5, 5.5), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplIntInt() = runComposeUiTest {
        var result1: State<Int>? = null
        var result2: State<Int>? = null
        setContent {
            result1 = useMaxImpl(3, 5)
            result2 = useMaxImpl(5, 3)
        }
        waitForIdle()
        assertEquals(max(3, 5), result1!!.value)
        assertEquals(max(5, 3), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplLongLong() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useMaxImpl(3L, 5L)
        }
        waitForIdle()
        assertEquals(max(3L, 5L), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplFloatFloat() = runComposeUiTest {
        var result: State<Float>? = null
        setContent {
            result = useMaxImpl(3.5f, 5.5f)
        }
        waitForIdle()
        assertEquals(max(3.5f, 5.5f), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplDoubleDouble() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useMaxImpl(3.5, 5.5)
        }
        waitForIdle()
        assertEquals(max(3.5, 5.5), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplIntLong() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useMaxImpl(3, 5L)
        }
        waitForIdle()
        assertEquals(max(3L, 5L), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplLongInt() = runComposeUiTest {
        var result: State<Long>? = null
        setContent {
            result = useMaxImpl(3L, 5)
        }
        waitForIdle()
        assertEquals(max(3L, 5L), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplFloatDouble() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useMaxImpl(3.5f, 5.5)
        }
        waitForIdle()
        assertEquals(max(3.5, 5.5), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplDoubleFloat() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useMaxImpl(3.5, 5.5f)
        }
        waitForIdle()
        assertEquals(max(3.5, 5.5), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplDoubleDouble() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2.0, 3.0)
        }
        waitForIdle()
        assertEquals(2.0.pow(3.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplDoubleInt() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2.0, 3)
        }
        waitForIdle()
        assertEquals(2.0.pow(3), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplDoubleFloat() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2.0, 3.0f)
        }
        waitForIdle()
        assertEquals(2.0.pow(3.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplFloatFloat() = runComposeUiTest {
        var result: State<Float>? = null
        setContent {
            result = usePowImpl(2.0f, 3.0f)
        }
        waitForIdle()
        assertEquals(2.0f.pow(3.0f), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplFloatInt() = runComposeUiTest {
        var result: State<Float>? = null
        setContent {
            result = usePowImpl(2.0f, 3)
        }
        waitForIdle()
        assertEquals(2.0f.pow(3), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplFloatDouble() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2.0f, 3.0)
        }
        waitForIdle()
        assertEquals(2.0.toDouble().pow(3.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplIntInt() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2, 3)
        }
        waitForIdle()
        assertEquals(2.toDouble().pow(3), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplIntDouble() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2, 3.0)
        }
        waitForIdle()
        assertEquals(2.toDouble().pow(3.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplIntFloat() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2, 3.0f)
        }
        waitForIdle()
        assertEquals(2.toDouble().pow(3.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplLongInt() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2L, 3)
        }
        waitForIdle()
        assertEquals(2L.toDouble().pow(3), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplLongDouble() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2L, 3.0)
        }
        waitForIdle()
        assertEquals(2L.toDouble().pow(3.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplLongFloat() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2L, 3.0f)
        }
        waitForIdle()
        assertEquals(2L.toDouble().pow(3.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplZeroExponent() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(5.0, 0)
        }
        waitForIdle()
        assertEquals(1.0, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplNegativeExponent() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(2.0, -3.0)
        }
        waitForIdle()
        assertEquals(2.0.pow(-3.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseSqrtImplDouble() = runComposeUiTest {
        var result1: State<Double>? = null
        var result2: State<Double>? = null
        setContent {
            result1 = useSqrtImpl(9.0)
            result2 = useSqrtImpl(0.0)
        }
        waitForIdle()
        assertEquals(sqrt(9.0), result1!!.value)
        assertEquals(sqrt(0.0), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseSqrtImplFloat() = runComposeUiTest {
        var result: State<Float>? = null
        setContent {
            result = useSqrtImpl(9.0f)
        }
        waitForIdle()
        assertEquals(sqrt(9.0f), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseSqrtImplInt() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useSqrtImpl(9)
        }
        waitForIdle()
        assertEquals(sqrt(9.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseSqrtImplLong() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useSqrtImpl(9L)
        }
        waitForIdle()
        assertEquals(sqrt(9.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplEqualValues() = runComposeUiTest {
        var result: State<Int>? = null
        setContent {
            result = useMinImpl(5, 5)
        }
        waitForIdle()
        assertEquals(5, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplEqualValues() = runComposeUiTest {
        var result: State<Int>? = null
        setContent {
            result = useMaxImpl(5, 5)
        }
        waitForIdle()
        assertEquals(5, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMinImplNegativeValues() = runComposeUiTest {
        var result: State<Int>? = null
        setContent {
            result = useMinImpl(-3, -5)
        }
        waitForIdle()
        assertEquals(min(-3, -5), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseMaxImplNegativeValues() = runComposeUiTest {
        var result: State<Int>? = null
        setContent {
            result = useMaxImpl(-3, -5)
        }
        waitForIdle()
        assertEquals(max(-3, -5), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseAbsImplLargeValues() = runComposeUiTest {
        var result1: State<Int>? = null
        var result2: State<Long>? = null
        setContent {
            result1 = useAbsImpl(Int.MIN_VALUE)
            result2 = useAbsImpl(Long.MIN_VALUE)
        }
        waitForIdle()
        assertEquals(abs(Int.MIN_VALUE), result1!!.value)
        assertEquals(abs(Long.MIN_VALUE), result2!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseSqrtImplPerfectSquare() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useSqrtImpl(16)
        }
        waitForIdle()
        assertEquals(4.0, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUseSqrtImplNonPerfectSquare() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = useSqrtImpl(2)
        }
        waitForIdle()
        assertEquals(sqrt(2.0), result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplFloatIntEdge() = runComposeUiTest {
        var result: State<Float>? = null
        setContent {
            result = usePowImpl(3.0f, 0)
        }
        waitForIdle()
        assertEquals(1.0f, result!!.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testUsePowImplLongIntEdge() = runComposeUiTest {
        var result: State<Double>? = null
        setContent {
            result = usePowImpl(3L, 0)
        }
        waitForIdle()
        assertEquals(1.0, result!!.value)
    }
}
