package xyz.junerver.compose.hooks.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/*
  Description: Easy access to dynamic options
  Author: Junerver
  Date: 2025/7/15-9:09
  Email: junerver@gmail.com
  Version: v1.0
*/
internal typealias OptionsOf<T> = T.() -> Unit
internal typealias Creator<T> = (optionsOf: OptionsOf<T>) -> T

@Composable
internal inline fun <T> useDynamicOptions(noinline optionsOf: OptionsOf<T>, crossinline creator: Creator<T>): T {
    return remember { creator(optionsOf) }.apply(optionsOf)
}
