package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.math.truncate

/*
  Description: Math related reactive state hooks
  Author: Junerver
  Date: 2025/7/3-19:42
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Returns a reactive state of the absolute value of the input number.
 *
 * @param number The input integer
 * @return A reactive state containing the absolute value
 */
@Composable
fun useAbs(number: Int): State<Int> = useState(number) {
    abs(number)
}

/**
 * Returns a reactive state of the absolute value of the input number.
 *
 * @param number The input double
 * @return A reactive state containing the absolute value
 */
@Composable
fun useAbs(number: Double): State<Double> = useState(number) {
    abs(number)
}

/**
 * Returns a reactive state of the absolute value of the input number.
 *
 * @param number The input float
 * @return A reactive state containing the absolute value
 */
@Composable
fun useAbs(number: Float): State<Float> = useState(number) {
    abs(number)
}

/**
 * Returns a reactive state of the absolute value of the input number.
 *
 * @param number The input long
 * @return A reactive state containing the absolute value
 */
@Composable
fun useAbs(number: Long): State<Long> = useState(number) {
    abs(number)
}

/**
 * Returns a reactive state of the ceiling value of the input number.
 * Rounds the given value to an integer towards positive infinity.
 *
 * @param number The input double
 * @return A reactive state containing the ceiling value
 */
@Composable
fun useCeil(number: Double): State<Double> = useState(number) {
    ceil(number)
}

/**
 * Returns a reactive state of the ceiling value of the input number.
 * Rounds the given value to an integer towards positive infinity.
 *
 * @param number The input float
 * @return A reactive state containing the ceiling value
 */
@Composable
fun useCeil(number: Float): State<Float> = useState(number) {
    ceil(number)
}

/**
 * Returns a reactive state of the ceiling value of the input number.
 * For integer types, returns the original value as ceiling operation has no effect.
 *
 * @param number The input integer
 * @return A reactive state containing the original value
 */
@Composable
fun useCeil(number: Int): State<Int> = useState(number) {
    number
}

/**
 * Returns a reactive state of the ceiling value of the input number.
 * For long types, returns the original value as ceiling operation has no effect.
 *
 * @param number The input long
 * @return A reactive state containing the original value
 */
@Composable
fun useCeil(number: Long): State<Long> = useState(number) {
    number
}

/**
 * Returns a reactive state of the floor value of the input number.
 * Rounds the given value to an integer towards negative infinity.
 *
 * @param number The input double
 * @return A reactive state containing the floor value
 */
@Composable
fun useFloor(number: Double): State<Double> = useState(number) {
    floor(number)
}

/**
 * Returns a reactive state of the floor value of the input number.
 * Rounds the given value to an integer towards negative infinity.
 *
 * @param number The input float
 * @return A reactive state containing the floor value
 */
@Composable
fun useFloor(number: Float): State<Float> = useState(number) {
    floor(number)
}

/**
 * Returns a reactive state of the floor value of the input number.
 * For integer types, returns the original value as floor operation has no effect.
 *
 * @param number The input integer
 * @return A reactive state containing the original value
 */
@Composable
fun useFloor(number: Int): State<Int> = useState(number) {
    number
}

/**
 * Returns a reactive state of the floor value of the input number.
 * For long types, returns the original value as floor operation has no effect.
 *
 * @param number The input long
 * @return A reactive state containing the original value
 */
@Composable
fun useFloor(number: Long): State<Long> = useState(number) {
    number
}

/**
 * Returns a reactive state of the rounded value of the input number.
 * Rounds the given value to the nearest integer.
 *
 * @param number The input double
 * @return A reactive state containing the rounded value
 */
@Composable
fun useRound(number: Double): State<Double> = useState(number) {
    round(number).toDouble()
}

/**
 * Returns a reactive state of the rounded value of the input number.
 * Rounds the given value to the nearest integer.
 *
 * @param number The input float
 * @return A reactive state containing the rounded value
 */
@Composable
fun useRound(number: Float): State<Float> = useState(number) {
    round(number).toFloat()
}

/**
 * Returns a reactive state of the rounded value of the input number.
 * For integer types, returns the original value as round operation has no effect.
 *
 * @param number The input integer
 * @return A reactive state containing the original value
 */
@Composable
fun useRound(number: Int): State<Int> = useState(number) {
    number
}

/**
 * Returns a reactive state of the rounded value of the input number.
 * For long types, returns the original value as round operation has no effect.
 *
 * @param number The input long
 * @return A reactive state containing the original value
 */
@Composable
fun useRound(number: Long): State<Long> = useState(number) {
    number
}

/**
 * Returns a reactive state of the truncated value of the input number.
 * Rounds the given value towards zero.
 *
 * @param number The input double
 * @return A reactive state containing the truncated value
 */
@Composable
fun useTrunc(number: Double): State<Double> = useState(number) {
    truncate(number)
}

/**
 * Returns a reactive state of the truncated value of the input number.
 * Rounds the given value towards zero.
 *
 * @param number The input float
 * @return A reactive state containing the truncated value
 */
@Composable
fun useTrunc(number: Float): State<Float> = useState(number) {
    truncate(number)
}

/**
 * Returns a reactive state of the truncated value of the input number.
 * For integer types, returns the original value as truncate operation has no effect.
 *
 * @param number The input integer
 * @return A reactive state containing the original value
 */
@Composable
fun useTrunc(number: Int): State<Int> = useState(number) {
    number
}

/**
 * Returns a reactive state of the truncated value of the input number.
 * For long types, returns the original value as truncate operation has no effect.
 *
 * @param number The input long
 * @return A reactive state containing the original value
 */
@Composable
fun useTrunc(number: Long): State<Long> = useState(number) {
    number
}

/**
 * Returns a reactive state of the minimum value between two integers.
 *
 * @param a The first integer
 * @param b The second integer
 * @return A reactive state containing the minimum value
 */
@Composable
fun useMin(a: Int, b: Int): State<Int> = useState(a, b) {
    min(a, b)
}

/**
 * Returns a reactive state of the minimum value between two longs.
 *
 * @param a The first long
 * @param b The second long
 * @return A reactive state containing the minimum value
 */
@Composable
fun useMin(a: Long, b: Long): State<Long> = useState(a, b) {
    min(a, b)
}

/**
 * Returns a reactive state of the minimum value between two floats.
 *
 * @param a The first float
 * @param b The second float
 * @return A reactive state containing the minimum value
 */
@Composable
fun useMin(a: Float, b: Float): State<Float> = useState(a, b) {
    min(a, b)
}

/**
 * Returns a reactive state of the minimum value between two doubles.
 *
 * @param a The first double
 * @param b The second double
 * @return A reactive state containing the minimum value
 */
@Composable
fun useMin(a: Double, b: Double): State<Double> = useState(a, b) {
    min(a, b)
}

/**
 * Returns a reactive state of the minimum value between an integer and a long.
 *
 * @param a The integer value
 * @param b The long value
 * @return A reactive state containing the minimum value as a long
 */
@Composable
fun useMin(a: Int, b: Long): State<Long> = useState(a, b) {
    min(a.toLong(), b)
}

/**
 * Returns a reactive state of the minimum value between a long and an integer.
 *
 * @param a The long value
 * @param b The integer value
 * @return A reactive state containing the minimum value as a long
 */
@Composable
fun useMin(a: Long, b: Int): State<Long> = useState(a, b) {
    min(a, b.toLong())
}

/**
 * Returns a reactive state of the minimum value between a float and a double.
 *
 * @param a The float value
 * @param b The double value
 * @return A reactive state containing the minimum value as a double
 */
@Composable
fun useMin(a: Float, b: Double): State<Double> = useState(a, b) {
    min(a.toDouble(), b)
}

/**
 * Returns a reactive state of the minimum value between a double and a float.
 *
 * @param a The double value
 * @param b The float value
 * @return A reactive state containing the minimum value as a double
 */
@Composable
fun useMin(a: Double, b: Float): State<Double> = useState(a, b) {
    min(a, b.toDouble())
}

/**
 * Returns a reactive state of the maximum value between two integers.
 *
 * @param a The first integer
 * @param b The second integer
 * @return A reactive state containing the maximum value
 */
@Composable
fun useMax(a: Int, b: Int): State<Int> = useState(a, b) {
    max(a, b)
}

/**
 * Returns a reactive state of the maximum value between two longs.
 *
 * @param a The first long
 * @param b The second long
 * @return A reactive state containing the maximum value
 */
@Composable
fun useMax(a: Long, b: Long): State<Long> = useState(a, b) {
    max(a, b)
}

/**
 * Returns a reactive state of the maximum value between two floats.
 *
 * @param a The first float
 * @param b The second float
 * @return A reactive state containing the maximum value
 */
@Composable
fun useMax(a: Float, b: Float): State<Float> = useState(a, b) {
    max(a, b)
}

/**
 * Returns a reactive state of the maximum value between two doubles.
 *
 * @param a The first double
 * @param b The second double
 * @return A reactive state containing the maximum value
 */
@Composable
fun useMax(a: Double, b: Double): State<Double> = useState(a, b) {
    max(a, b)
}

/**
 * Returns a reactive state of the maximum value between an integer and a long.
 *
 * @param a The integer value
 * @param b The long value
 * @return A reactive state containing the maximum value as a long
 */
@Composable
fun useMax(a: Int, b: Long): State<Long> = useState(a, b) {
    max(a.toLong(), b)
}

/**
 * Returns a reactive state of the maximum value between a long and an integer.
 *
 * @param a The long value
 * @param b The integer value
 * @return A reactive state containing the maximum value as a long
 */
@Composable
fun useMax(a: Long, b: Int): State<Long> = useState(a, b) {
    max(a, b.toLong())
}

/**
 * Returns a reactive state of the maximum value between a float and a double.
 *
 * @param a The float value
 * @param b The double value
 * @return A reactive state containing the maximum value as a double
 */
@Composable
fun useMax(a: Float, b: Double): State<Double> = useState(a, b) {
    max(a.toDouble(), b)
}

/**
 * Returns a reactive state of the maximum value between a double and a float.
 *
 * @param a The double value
 * @param b The float value
 * @return A reactive state containing the maximum value as a double
 */
@Composable
fun useMax(a: Double, b: Float): State<Double> = useState(a, b) {
    max(a, b.toDouble())
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the double base raised to the power of the double exponent.
 *
 * @param base The double base number
 * @param exponent The double exponent
 * @return A reactive state containing the power operation result
 */
@Composable
fun usePow(base: Double, exponent: Double): State<Double> = useState(base, exponent) {
    base.pow(exponent)
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the double base raised to the power of the integer exponent.
 *
 * @param base The double base number
 * @param exponent The integer exponent
 * @return A reactive state containing the power operation result
 */
@Composable
fun usePow(base: Double, exponent: Int): State<Double> = useState(base, exponent) {
    base.pow(exponent)
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the double base raised to the power of the float exponent.
 *
 * @param base The double base number
 * @param exponent The float exponent
 * @return A reactive state containing the power operation result
 */
@Composable
fun usePow(base: Double, exponent: Float): State<Double> = useState(base, exponent) {
    base.pow(exponent.toDouble())
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the float base raised to the power of the float exponent.
 *
 * @param base The float base number
 * @param exponent The float exponent
 * @return A reactive state containing the power operation result
 */
@Composable
fun usePow(base: Float, exponent: Float): State<Float> = useState(base, exponent) {
    base.pow(exponent)
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the float base raised to the power of the integer exponent.
 *
 * @param base The float base number
 * @param exponent The integer exponent
 * @return A reactive state containing the power operation result
 */
@Composable
fun usePow(base: Float, exponent: Int): State<Float> = useState(base, exponent) {
    base.pow(exponent)
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the float base raised to the power of the double exponent.
 *
 * @param base The float base number
 * @param exponent The double exponent
 * @return A reactive state containing the double power operation result
 */
@Composable
fun usePow(base: Float, exponent: Double): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the integer base raised to the power of the integer exponent.
 *
 * @param base The integer base number
 * @param exponent The integer exponent
 * @return A reactive state containing the double power operation result
 */
@Composable
fun usePow(base: Int, exponent: Int): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the integer base raised to the power of the double exponent.
 *
 * @param base The integer base number
 * @param exponent The double exponent
 * @return A reactive state containing the double power operation result
 */
@Composable
fun usePow(base: Int, exponent: Double): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the integer base raised to the power of the float exponent.
 *
 * @param base The integer base number
 * @param exponent The float exponent
 * @return A reactive state containing the double power operation result
 */
@Composable
fun usePow(base: Int, exponent: Float): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent.toDouble())
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the long base raised to the power of the integer exponent.
 *
 * @param base The long base number
 * @param exponent The integer exponent
 * @return A reactive state containing the double power operation result
 */
@Composable
fun usePow(base: Long, exponent: Int): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the long base raised to the power of the double exponent.
 *
 * @param base The long base number
 * @param exponent The double exponent
 * @return A reactive state containing the double power operation result
 */
@Composable
fun usePow(base: Long, exponent: Double): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

/**
 * Returns a reactive state of the power operation result.
 * Calculates the long base raised to the power of the float exponent.
 *
 * @param base The long base number
 * @param exponent The float exponent
 * @return A reactive state containing the double power operation result
 */
@Composable
fun usePow(base: Long, exponent: Float): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent.toDouble())
}

/**
 * Returns a reactive state of the square root of the input double.
 *
 * @param number The input double
 * @return A reactive state containing the square root value
 */
@Composable
fun useSqrt(number: Double): State<Double> = useState(number) {
    sqrt(number)
}

/**
 * Returns a reactive state of the square root of the input float.
 *
 * @param number The input float
 * @return A reactive state containing the square root value
 */
@Composable
fun useSqrt(number: Float): State<Float> = useState(number) {
    sqrt(number)
}

/**
 * Returns a reactive state of the square root of the input integer.
 *
 * @param number The input integer
 * @return A reactive state containing the square root value as a double
 */
@Composable
fun useSqrt(number: Int): State<Double> = useState(number) {
    sqrt(number.toDouble())
}

/**
 * Returns a reactive state of the square root of the input long.
 *
 * @param number The input long
 * @return A reactive state containing the square root value as a double
 */
@Composable
fun useSqrt(number: Long): State<Double> = useState(number) {
    sqrt(number.toDouble())
}
