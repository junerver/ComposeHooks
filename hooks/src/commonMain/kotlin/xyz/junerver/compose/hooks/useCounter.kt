package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import xyz.junerver.kotlin.Tuple5
import xyz.junerver.kotlin.tuple

/*
  Description: A hook that manage counter.
  Author: Junerver
  Date: 2024/7/8-13:17
  Email: junerver@gmail.com
  Version: v1.0
*/

data class CounterOptions internal constructor(
    var min: Int = 0,
    var max: Int = 10,
) {
    companion object : Options<CounterOptions>(::CounterOptions)
}

internal typealias IncFn = (Int) -> Unit
internal typealias DecFn = (Int) -> Unit

/**
 * Use counter
 *
 * @param initialValue initial value of count
 * @param options
 * @return
 *
 * 在使用第三个解构值[SetValueFn]时，如果你想传入的是数值则使用 [left] 函数， 如果想传入的是函数 则使用 [right]
 * 函数，也可以手动导入[invoke]来优化使用体验。
 *
 * When using the third destructured value [SetValueFn], if you want to
 * pass in a numerical value, use the [left] function. If you want to pass
 * in a function, use the [right] function. You can also manually import
 * [invoke] to optimize the user experience.
 *
 * ```
 * set(3.left())
 * set({value:Int ->
 *      value/3
 * }.right())
 * ```
 */
@Composable
fun useCounter(
    initialValue: Int = 0,
    options: CounterOptions,
): Tuple5<Int, IncFn, DecFn, SetValueFn<Either<Int, (Int) -> Int>>, ResetFn> {
    val (current, setCurrent, getCurrent) = useGetState(getTargetValue(initialValue, options))
    val setValue: SetValueFn<Either<Int, (Int) -> Int>> = { value: Either<Int, (Int) -> Int> ->
        val target = value.fold(
            ifLeft = { it },
            ifRight = { it(getCurrent()) }
        )
        setCurrent(getTargetValue(target, options))
    }

    fun inc(delta: Int) {
        setValue { c: Int -> c + delta }
    }

    fun dec(delta: Int) {
        setValue { c: Int -> c - delta }
    }

    fun set(value: Either<Int, (Int) -> Int>) {
        setValue(value)
    }

    fun reset() {
        setValue(initialValue)
    }

    return tuple(
        current,
        ::inc,
        ::dec,
        ::set,
        ::reset
    )
}

private fun getTargetValue(value: Int, options: CounterOptions): Int {
    val (min, max) = options
    return value.coerceIn(min, max)
}

operator fun SetValueFn<Either<Int, (Int) -> Int>>.invoke(leftValue: Int) = this(leftValue.left())
operator fun SetValueFn<Either<Int, (Int) -> Int>>.invoke(rightValue: (Int) -> Int) =
    this(rightValue.right())
