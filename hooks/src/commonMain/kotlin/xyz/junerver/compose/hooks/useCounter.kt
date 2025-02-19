package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import arrow.core.Either
import arrow.core.left
import arrow.core.right

/*
  Description: A hook that manage counter.
  Author: Junerver
  Date: 2024/7/8-13:17
  Email: junerver@gmail.com
  Version: v1.0
*/
@Stable
data class CounterOptions internal constructor(
    var min: Int = 0,
    var max: Int = 10,
) {
    companion object : Options<CounterOptions>(::CounterOptions)
}

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
private fun useCounter(initialValue: Int = 0, options: CounterOptions): CounterHolder {
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

    return remember {
        CounterHolder(
            current,
            ::inc,
            ::dec,
            ::set,
            ::reset
        )
    }
}

@Composable
fun useCounter(initialValue: Int = 0, optionsOf: CounterOptions.() -> Unit) =
    useCounter(initialValue, remember { CounterOptions.optionOf(optionsOf) })

private fun getTargetValue(value: Int, options: CounterOptions): Int {
    val (min, max) = options
    return value.coerceIn(min, max)
}

@Stable
data class CounterHolder(
    val state: State<Int>,
    val inc: IncFn,
    val dec: DecFn,
    val setValue: SetValueFn<SetterEither<Int>>,
    val reset: ResetFn,
)
