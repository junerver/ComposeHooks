@file:Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")

package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.userequest.RequestOptions

/**
 * Description: 规范Options的形式
 *
 * 需要作为选项配置的 `data class` 只需要添加一个伴生对象即可：
 *
 * ```
 *  companion object : Options<DebounceOptions>(::DebounceOptions)
 * ```
 *
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class Options<T>(val creator: () -> T) {
    /** [optionOf]函数通过[apply]构造模式来修改默认参数对象 */
    fun optionOf(opt: T.() -> Unit): T = creator().apply {
        opt()
    }
}

private inline fun <reified T> createOptions(noinline opt: T.() -> Unit): T = when (T::class) {
    CountdownOptions::class -> CountdownOptions.optionOf(opt as CountdownOptions.() -> Unit)
    CounterOptions::class -> CounterOptions.optionOf(opt as CounterOptions.() -> Unit)
    DebounceOptions::class -> DebounceOptions.optionOf(opt as DebounceOptions.() -> Unit)
    IntervalOptions::class -> IntervalOptions.optionOf(opt as IntervalOptions.() -> Unit)
    UseNowOptions::class -> UseNowOptions.optionOf(opt as UseNowOptions.() -> Unit)
    ThrottleOptions::class -> ThrottleOptions.optionOf(opt as ThrottleOptions.() -> Unit)
    TimestampOptions::class -> TimestampOptions.optionOf(opt as TimestampOptions.() -> Unit)
    RequestOptions::class -> RequestOptions.optionOf(opt as RequestOptions<Any>.() -> Unit)
    UseDateFormatOptions::class -> UseDateFormatOptions.optionOf(opt as UseDateFormatOptions.() -> Unit)
    StateAsyncOptions::class -> StateAsyncOptions.optionOf(opt as StateAsyncOptions.() -> Unit)
    UseTimeAgoOptions::class -> UseTimeAgoOptions.optionOf(opt as UseTimeAgoOptions.() -> Unit)
    TimeoutFnOptions::class -> TimeoutFnOptions.optionOf(opt as TimeoutFnOptions.() -> Unit)
    UseTimeoutPollOptions::class -> UseTimeoutPollOptions.optionOf(opt as UseTimeoutPollOptions.() -> Unit)
    UseCycleListOptions::class -> UseCycleListOptions.optionOf(opt as UseCycleListOptions<Any>.() -> Unit)
    UseSortedOptions::class -> UseSortedOptions.optionOf(opt as UseSortedOptions<Any>.() -> Unit)
    else -> error("unsupported options!!!!")
} as T

internal typealias OptionsOf<T> = T.() -> Unit
internal typealias Creator<T> = (optionsOf: OptionsOf<T>) -> T

@Composable
internal inline fun <reified T> useDynamicOptions(noinline optionsOf: OptionsOf<T>): T =
    remember { createOptions(optionsOf) }.apply(optionsOf)

@Composable
internal inline fun <T> useDynamicOptions(noinline optionsOf: OptionsOf<T>, crossinline creator: Creator<T>): T =
    remember { creator(optionsOf) }.apply(optionsOf)
