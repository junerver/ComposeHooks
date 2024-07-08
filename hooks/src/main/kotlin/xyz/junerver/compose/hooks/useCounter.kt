package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import arrow.core.Either
import arrow.core.Tuple5
import arrow.core.left
import arrow.core.right

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

@Composable
fun useCounter(initialValue: Int = 0, options: CounterOptions): Tuple5<Int, IncFn, DecFn, SetValueFn<Either<Int, (Int) -> Int>>, ResetFn> {
    val (current, setCurrent, getCurrent) = useGetState(getTargetValue(initialValue, options))
    fun setValue(value: Either<Int, (Int) -> Int>) {
        val target = value.fold(
            ifLeft = { it },
            ifRight = { it(getCurrent()) }
        )
        setCurrent(getTargetValue(target, options))
    }

    fun inc(delta: Int) {
        setValue({ c: Int -> c + delta }.right())
    }

    fun dec(delta: Int) {
        setValue({ c: Int -> c - delta }.right())
    }

    fun set(value: Either<Int, (Int) -> Int>) {
        setValue(value)
    }
    fun reset() {
        setValue(initialValue.left())
    }

    return Tuple5(
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
