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
 * 使用的时候也非常方便，直接通过[optionsOf]即可创建选项实例。
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class Options<T>(val creator: () -> T) {
    /** [optionOf]函数通过[apply]构造模式来修改默认参数对象 */
    fun optionOf(opt: T.() -> Unit): T = creator().apply {
        opt()
    }
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
