@file:Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")

package xyz.junerver.compose.hooks

import xyz.junerver.compose.hooks.userequest.RequestOptions

/*
  Description:
  Author: Junerver
  Date: 2024/9/12-10:40
  Email: junerver@gmail.com
  Version: v1.0
*/

actual inline fun <reified T> optionsOf(noinline opt: T.() -> Unit): T {
    return when (T::class) {
        CountdownOptions::class -> CountdownOptions.optionOf(opt as CountdownOptions.() -> Unit)
        CounterOptions::class -> CounterOptions.optionOf(opt as CounterOptions.() -> Unit)
        DebounceOptions::class -> DebounceOptions.optionOf(opt as DebounceOptions.() -> Unit)
        IntervalOptions::class -> IntervalOptions.optionOf(opt as IntervalOptions.() -> Unit)
        UseNowOptions::class -> UseNowOptions.optionOf(opt as UseNowOptions.() -> Unit)
        ThrottleOptions::class -> ThrottleOptions.optionOf(opt as ThrottleOptions.() -> Unit)
        TimestampOptions::class -> TimestampOptions.optionOf(opt as TimestampOptions.() -> Unit)
        RequestOptions::class -> RequestOptions.optionOf<Any> { opt as RequestOptions<Any>.() -> Unit }
        else -> {
            error("unsupported options!!!!")
        }
    } as T
}

actual inline fun <reified T> defaultOption(): T {
    return when (T::class) {
        CountdownOptions::class -> CountdownOptions.default()
        CounterOptions::class -> CounterOptions.default()
        DebounceOptions::class -> DebounceOptions.default()
        IntervalOptions::class -> IntervalOptions.default()
        UseNowOptions::class -> UseNowOptions.default()
        ThrottleOptions::class -> ThrottleOptions.default()
        TimestampOptions::class -> TimestampOptions.default()
        RequestOptions::class -> RequestOptions.default<Any>()
        else -> {
            error("unsupported options!!!!")
        }
    } as T
}
