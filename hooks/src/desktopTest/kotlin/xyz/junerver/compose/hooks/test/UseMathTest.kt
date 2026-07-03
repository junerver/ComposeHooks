package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import xyz.junerver.compose.hooks.useAbs
import xyz.junerver.compose.hooks.useCeil
import xyz.junerver.compose.hooks.useFloor
import xyz.junerver.compose.hooks.useMax
import xyz.junerver.compose.hooks.useMin
import xyz.junerver.compose.hooks.usePow
import xyz.junerver.compose.hooks.useRound
import xyz.junerver.compose.hooks.useSqrt
import xyz.junerver.compose.hooks.useTrunc

@Suppress("DEPRECATION")
class UseMathTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun abs_recomputes_for_integral_and_fractional_inputs() = runComposeUiTest {
        var intInput by mutableStateOf(-5)
        var longInput by mutableStateOf(-100L)
        var floatInput by mutableStateOf(-1.5f)
        var doubleInput by mutableStateOf(-3.14)

        lateinit var absInt: State<Int>
        lateinit var absLong: State<Long>
        lateinit var absFloat: State<Float>
        lateinit var absDouble: State<Double>

        setContent {
            absInt = useAbs(intInput)
            absLong = useAbs(longInput)
            absFloat = useAbs(floatInput)
            absDouble = useAbs(doubleInput)
        }

        waitForIdle()
        assertEquals(5, absInt.value)
        assertEquals(100L, absLong.value)
        assertEquals(1.5f, absFloat.value)
        assertEquals(3.14, absDouble.value)

        intInput = 0
        longInput = 42L
        floatInput = 2.5f
        doubleInput = 2.71
        waitForIdle()
        assertEquals(0, absInt.value)
        assertEquals(42L, absLong.value)
        assertEquals(2.5f, absFloat.value)
        assertEquals(2.71, absDouble.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ceil_floor_round_and_trunc_follow_input_changes() = runComposeUiTest {
        var doubleInput by mutableStateOf(2.9)
        var floatInput by mutableStateOf(-2.5f)
        var intInput by mutableStateOf(5)
        var longInput by mutableStateOf(10L)

        lateinit var ceilDouble: State<Double>
        lateinit var floorDouble: State<Double>
        lateinit var roundFloat: State<Float>
        lateinit var truncDouble: State<Double>
        lateinit var ceilInt: State<Int>
        lateinit var floorLong: State<Long>

        setContent {
            ceilDouble = useCeil(doubleInput)
            floorDouble = useFloor(doubleInput)
            roundFloat = useRound(floatInput)
            truncDouble = useTrunc(doubleInput)
            ceilInt = useCeil(intInput)
            floorLong = useFloor(longInput)
        }

        waitForIdle()
        assertEquals(ceil(2.9), ceilDouble.value)
        assertEquals(floor(2.9), floorDouble.value)
        assertEquals(round(-2.5f), roundFloat.value)
        assertEquals(truncate(2.9), truncDouble.value)
        assertEquals(5, ceilInt.value)
        assertEquals(10L, floorLong.value)

        doubleInput = -2.1
        floatInput = 2.5f
        intInput = 7
        longInput = 12L
        waitForIdle()
        assertEquals(ceil(-2.1), ceilDouble.value)
        assertEquals(floor(-2.1), floorDouble.value)
        assertEquals(round(2.5f), roundFloat.value)
        assertEquals(truncate(-2.1), truncDouble.value)
        assertEquals(7, ceilInt.value)
        assertEquals(12L, floorLong.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun min_and_max_recompute_for_same_type_and_mixed_type_inputs() = runComposeUiTest {
        var intA by mutableStateOf(3)
        var intB by mutableStateOf(5)
        var mixedInt by mutableStateOf(3)
        var mixedLong by mutableStateOf(5L)
        var mixedFloat by mutableStateOf(3.5f)
        var mixedDouble by mutableStateOf(5.5)

        lateinit var minInt: State<Int>
        lateinit var maxInt: State<Int>
        lateinit var minMixed: State<Long>
        lateinit var maxMixed: State<Double>

        setContent {
            minInt = useMin(intA, intB)
            maxInt = useMax(intA, intB)
            minMixed = useMin(mixedInt, mixedLong)
            maxMixed = useMax(mixedFloat, mixedDouble)
        }

        waitForIdle()
        assertEquals(min(3, 5), minInt.value)
        assertEquals(max(3, 5), maxInt.value)
        assertEquals(min(3L, 5L), minMixed.value)
        assertEquals(max(3.5, 5.5), maxMixed.value)

        intA = 8
        intB = 2
        mixedInt = 9
        mixedLong = 4L
        mixedFloat = 8.5f
        mixedDouble = 2.5
        waitForIdle()
        assertEquals(min(8, 2), minInt.value)
        assertEquals(max(8, 2), maxInt.value)
        assertEquals(min(9L, 4L), minMixed.value)
        assertEquals(max(8.5, 2.5), maxMixed.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pow_supports_multiple_overloads_and_special_exponents() = runComposeUiTest {
        var doubleBase by mutableStateOf(2.0)
        var doubleExponent by mutableStateOf(3.0)
        var floatBase by mutableStateOf(2.0f)
        var intExponent by mutableStateOf(3)
        var intBase by mutableStateOf(2)
        var negativeExponent by mutableStateOf(-3.0)

        lateinit var powDouble: State<Double>
        lateinit var powFloat: State<Float>
        lateinit var powInt: State<Double>
        lateinit var powNegative: State<Double>

        setContent {
            powDouble = usePow(doubleBase, doubleExponent)
            powFloat = usePow(floatBase, intExponent)
            powInt = usePow(intBase, intExponent)
            powNegative = usePow(doubleBase, negativeExponent)
        }

        waitForIdle()
        assertEquals(2.0.pow(3.0), powDouble.value)
        assertEquals(2.0f.pow(3), powFloat.value)
        assertEquals(2.0.pow(3), powInt.value)
        assertEquals(2.0.pow(-3.0), powNegative.value)

        doubleBase = 5.0
        doubleExponent = 0.0
        floatBase = 3.0f
        intExponent = 0
        intBase = 3
        negativeExponent = -2.0
        waitForIdle()
        assertEquals(1.0, powDouble.value)
        assertEquals(1.0f, powFloat.value)
        assertEquals(1.0, powInt.value)
        assertEquals(5.0.pow(-2.0), powNegative.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun sqrt_recomputes_for_perfect_and_non_perfect_squares() = runComposeUiTest {
        var intInput by mutableStateOf(16)
        var longInput by mutableStateOf(9L)
        var floatInput by mutableStateOf(9.0f)
        var doubleInput by mutableStateOf(2.0)

        lateinit var sqrtInt: State<Double>
        lateinit var sqrtLong: State<Double>
        lateinit var sqrtFloat: State<Float>
        lateinit var sqrtDouble: State<Double>

        setContent {
            sqrtInt = useSqrt(intInput)
            sqrtLong = useSqrt(longInput)
            sqrtFloat = useSqrt(floatInput)
            sqrtDouble = useSqrt(doubleInput)
        }

        waitForIdle()
        assertEquals(4.0, sqrtInt.value)
        assertEquals(3.0, sqrtLong.value)
        assertEquals(3.0f, sqrtFloat.value)
        assertEquals(sqrt(2.0), sqrtDouble.value)

        intInput = 25
        longInput = 4L
        floatInput = 16.0f
        doubleInput = 81.0
        waitForIdle()
        assertEquals(5.0, sqrtInt.value)
        assertEquals(2.0, sqrtLong.value)
        assertEquals(4.0f, sqrtFloat.value)
        assertEquals(9.0, sqrtDouble.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun abs_preserves_current_standard_library_behavior_for_min_values() = runComposeUiTest {
        lateinit var absInt: State<Int>
        lateinit var absLong: State<Long>

        setContent {
            absInt = useAbs(Int.MIN_VALUE)
            absLong = useAbs(Long.MIN_VALUE)
        }

        waitForIdle()
        assertEquals(abs(Int.MIN_VALUE), absInt.value)
        assertEquals(abs(Long.MIN_VALUE), absLong.value)
    }
}
