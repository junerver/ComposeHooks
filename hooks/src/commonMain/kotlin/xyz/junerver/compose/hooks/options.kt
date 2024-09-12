@file:Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")

package xyz.junerver.compose.hooks

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
 * 使用的时候也非常方便，直接通过[optionsOf]或者[defaultOption]这两个函数即可创建选项实例。
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class Options<T>(val creator: () -> T) {
    /** [optionOf]函数通过[apply]构造模式来修改默认参数对象 */
    fun optionOf(opt: T.() -> Unit): T = creator().apply {
        opt()
    }

    /** [default]函数直接调用构造器函数获取默认实例 */
    fun default() = creator()
}

inline fun <reified T> optionsOf(noinline opt: T.() -> Unit): T {
    return when (T::class) {
        CountdownOptions::class -> CountdownOptions.optionOf(opt as CountdownOptions.() -> Unit)
        CounterOptions::class -> CounterOptions.optionOf(opt as CounterOptions.() -> Unit)
        DebounceOptions::class -> DebounceOptions.optionOf(opt as DebounceOptions.() -> Unit)
        IntervalOptions::class -> IntervalOptions.optionOf(opt as IntervalOptions.() -> Unit)
        UseNowOptions::class -> UseNowOptions.optionOf(opt as UseNowOptions.() -> Unit)
        ThrottleOptions::class -> ThrottleOptions.optionOf(opt as ThrottleOptions.() -> Unit)
        TimestampOptions::class -> TimestampOptions.optionOf(opt as TimestampOptions.() -> Unit)
        RequestOptions::class -> RequestOptions.optionOf(opt as RequestOptions<Any>.() -> Unit)
        else -> error("unsupported options!!!!")
    } as T
}

inline fun <reified T> defaultOption(): T {
    return when (T::class) {
        CountdownOptions::class -> CountdownOptions.default()
        CounterOptions::class -> CounterOptions.default()
        DebounceOptions::class -> DebounceOptions.default()
        IntervalOptions::class -> IntervalOptions.default()
        UseNowOptions::class -> UseNowOptions.default()
        ThrottleOptions::class -> ThrottleOptions.default()
        TimestampOptions::class -> TimestampOptions.default()
        RequestOptions::class -> RequestOptions.default<Any>()
        else -> error("unsupported options!!!!")
    } as T
}
