package xyz.junerver.compose.hooks.usemath

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
import xyz.junerver.compose.hooks.useState

/*
  Description: Math related reactive state hooks
  Author: Junerver
  Date: 2025/7/3-19:42
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useAbsImpl(number: Int): State<Int> = useState(number) {
    abs(number)
}

@Composable
fun useAbsImpl(number: Double): State<Double> = useState(number) {
    abs(number)
}

@Composable
fun useAbsImpl(number: Float): State<Float> = useState(number) {
    abs(number)
}

@Composable
fun useAbsImpl(number: Long): State<Long> = useState(number) {
    abs(number)
}

@Composable
fun useCeilImpl(number: Double): State<Double> = useState(number) {
    ceil(number)
}

@Composable
fun useCeilImpl(number: Float): State<Float> = useState(number) {
    ceil(number)
}

@Composable
fun useCeilImpl(number: Int): State<Int> = useState(number) {
    number
}

@Composable
fun useCeilImpl(number: Long): State<Long> = useState(number) {
    number
}

@Composable
fun useFloorImpl(number: Double): State<Double> = useState(number) {
    floor(number)
}

@Composable
fun useFloorImpl(number: Float): State<Float> = useState(number) {
    floor(number)
}

@Composable
fun useFloorImpl(number: Int): State<Int> = useState(number) {
    number
}

@Composable
fun useFloorImpl(number: Long): State<Long> = useState(number) {
    number
}

@Composable
fun useRoundImpl(number: Double): State<Double> = useState(number) {
    round(number)
}

@Composable
fun useRoundImpl(number: Float): State<Float> = useState(number) {
    round(number)
}

@Composable
fun useRoundImpl(number: Int): State<Int> = useState(number) {
    number
}

@Composable
fun useRoundImpl(number: Long): State<Long> = useState(number) {
    number
}

@Composable
fun useTruncImpl(number: Double): State<Double> = useState(number) {
    truncate(number)
}

@Composable
fun useTruncImpl(number: Float): State<Float> = useState(number) {
    truncate(number)
}

@Composable
fun useTruncImpl(number: Int): State<Int> = useState(number) {
    number
}

@Composable
fun useTruncImpl(number: Long): State<Long> = useState(number) {
    number
}

@Composable
fun useMinImpl(a: Int, b: Int): State<Int> = useState(a, b) {
    min(a, b)
}

@Composable
fun useMinImpl(a: Long, b: Long): State<Long> = useState(a, b) {
    min(a, b)
}

@Composable
fun useMinImpl(a: Float, b: Float): State<Float> = useState(a, b) {
    min(a, b)
}

@Composable
fun useMinImpl(a: Double, b: Double): State<Double> = useState(a, b) {
    min(a, b)
}

@Composable
fun useMinImpl(a: Int, b: Long): State<Long> = useState(a, b) {
    min(a.toLong(), b)
}

@Composable
fun useMinImpl(a: Long, b: Int): State<Long> = useState(a, b) {
    min(a, b.toLong())
}

@Composable
fun useMinImpl(a: Float, b: Double): State<Double> = useState(a, b) {
    min(a.toDouble(), b)
}

@Composable
fun useMinImpl(a: Double, b: Float): State<Double> = useState(a, b) {
    min(a, b.toDouble())
}

@Composable
fun useMaxImpl(a: Int, b: Int): State<Int> = useState(a, b) {
    max(a, b)
}

@Composable
fun useMaxImpl(a: Long, b: Long): State<Long> = useState(a, b) {
    max(a, b)
}

@Composable
fun useMaxImpl(a: Float, b: Float): State<Float> = useState(a, b) {
    max(a, b)
}

@Composable
fun useMaxImpl(a: Double, b: Double): State<Double> = useState(a, b) {
    max(a, b)
}

@Composable
fun useMaxImpl(a: Int, b: Long): State<Long> = useState(a, b) {
    max(a.toLong(), b)
}

@Composable
fun useMaxImpl(a: Long, b: Int): State<Long> = useState(a, b) {
    max(a, b.toLong())
}

@Composable
fun useMaxImpl(a: Float, b: Double): State<Double> = useState(a, b) {
    max(a.toDouble(), b)
}

@Composable
fun useMaxImpl(a: Double, b: Float): State<Double> = useState(a, b) {
    max(a, b.toDouble())
}

@Composable
fun usePowImpl(base: Double, exponent: Double): State<Double> = useState(base, exponent) {
    base.pow(exponent)
}

@Composable
fun usePowImpl(base: Double, exponent: Int): State<Double> = useState(base, exponent) {
    base.pow(exponent)
}

@Composable
fun usePowImpl(base: Double, exponent: Float): State<Double> = useState(base, exponent) {
    base.pow(exponent.toDouble())
}

@Composable
fun usePowImpl(base: Float, exponent: Float): State<Float> = useState(base, exponent) {
    base.pow(exponent)
}

@Composable
fun usePowImpl(base: Float, exponent: Int): State<Float> = useState(base, exponent) {
    base.pow(exponent)
}

@Composable
fun usePowImpl(base: Float, exponent: Double): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

@Composable
fun usePowImpl(base: Int, exponent: Int): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

@Composable
fun usePowImpl(base: Int, exponent: Double): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

@Composable
fun usePowImpl(base: Int, exponent: Float): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent.toDouble())
}

@Composable
fun usePowImpl(base: Long, exponent: Int): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

@Composable
fun usePowImpl(base: Long, exponent: Double): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent)
}

@Composable
fun usePowImpl(base: Long, exponent: Float): State<Double> = useState(base, exponent) {
    base.toDouble().pow(exponent.toDouble())
}

@Composable
fun useSqrtImpl(number: Double): State<Double> = useState(number) {
    sqrt(number)
}

@Composable
fun useSqrtImpl(number: Float): State<Float> = useState(number) {
    sqrt(number)
}

@Composable
fun useSqrtImpl(number: Int): State<Double> = useState(number) {
    sqrt(number.toDouble())
}

@Composable
fun useSqrtImpl(number: Long): State<Double> = useState(number) {
    sqrt(number.toDouble())
}
