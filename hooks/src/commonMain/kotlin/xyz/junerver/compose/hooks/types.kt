@file:Suppress("unused")

package xyz.junerver.compose.hooks

import kotlin.reflect.KFunction0
import kotlinx.coroutines.CoroutineScope

/*
  Description: Types
  Author: Junerver
  Date: 2024/2/2-8:02
  Email: junerver@gmail.com
  Version: v1.0
*/

// 原始函数模型 (TParams) -> TData
internal typealias TParams = Array<Any?> // 原函数可变长度的参数

// 对所有函数固定抽象
internal typealias NormalFunction<TData> = (TParams) -> TData
internal typealias SuspendNormalFunction<TData> = suspend (TParams) -> TData
internal typealias VoidFunction = NormalFunction<Unit>
internal typealias SuspendVoidFunction = SuspendNormalFunction<Unit>

// 最常规的函数 ()->Unit
internal typealias NoParamsVoidFunction = KFunction0<Unit>

/**
 * 优化函数调用形式，将导出的函数伪装成普通函数的样子，无需对参数进行[arrayOf]，可能需要手动导包：
 * ```
 * import xyz.junerver.compose.hooks.invoke
 * ```
 */
operator fun <TData> NormalFunction<TData>.invoke(vararg params: Any?) = this(arrayOf(*params))

operator fun VoidFunction.invoke(vararg params: Any?) = this(arrayOf(*params))

internal typealias PauseFn = KFunction0<Unit>
internal typealias ResumeFn = KFunction0<Unit>
internal typealias IsActive = Boolean

internal typealias ToggleFn = () -> Unit

internal typealias SetValueFn<T> = (T) -> Unit
internal typealias ResetFn = () -> Unit
internal typealias GetValueFn<T> = () -> T

internal typealias SuspendAsyncFn = suspend CoroutineScope.() -> Unit
