@file:Suppress("unused")

package xyz.junerver.compose.hooks

import kotlin.reflect.KFunction
import kotlin.reflect.KFunction0
import kotlin.reflect.full.callSuspend
import kotlinx.coroutines.CoroutineScope
import xyz.junerver.compose.hooks.utils.checkIsLegalParameters

/*
  Description:
  @author Junerver
  date: 2024/2/2-8:02
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

internal typealias DependencyList = Array<out Any>

/**
 * 用来将任意一个函数转换成 noop 函数。但是需要注意，如果这个函数是一个实例的函数，必须要传入对应的实例。
 *
 * 注意不能应用于 Composable 内部的函数，只能应用于外部的、非 Composable 的函数。
 *
 * 例如：
 * ```kotlin
 * interface TestInt {
 *     fun addadd(a: Int, b: Int)
 * }
 * class TestAbs() : TestInt {
 *     override fun addadd(a: Int, b: Int) {
 *         println("实现类：" + (a + b))
 *     }
 * }
 * @Test
 * fun testCallWrapFn() {
 *     TestAbs().addadd(1, 2)
 *     // 第一个参数是实例
 *     TestInt::addadd.toNoopFn(TestAbs())(arrayOf(4, 5))
 *     TestInt::addadd.toNoopFn()(arrayOf(TestAbs(), 4, 5))
 * }
 * ```
 *
 * 注意 Compose 编译魔法！例如一个 Composable 内的简单的局部函数：
 * ```
 * fun add() {
 *   ++count
 * }
 * ```
 * 他看起来是一个无参数的普通函数，如果我们转换成 [NormalFunction] 将会出现运行时错误。
 * 经过 Compose 插件编译后，这个函数的实际签名是：
 * Function 'add' (JVM signature: TestUndoScreen$add(Landroidx/compose/runtime/MutableIntState;)V)，
 * 它变成了`TestUndoScreen$add`，而且还多了一个参数，意味着我们在调用 noop 时将会缺少实例
 * (Composable 是个函数，我们拿不到其实例)，从而抛出异常。
 *
 * @param instance 对应函数的实例，如果是顶层函数可以不传递。
 *
 */
fun <T> KFunction<T?>.asNoopFn(instance: Any? = null): (TParams) -> T? = fun(params: TParams): T? =
    this.call(*synthesisParametersAndCheck(instance, params, this))

/**
 * 将一个 [suspend] 挂起函数转换成[suspend] 版本的 [NormalFunction] 函数。
 * 直接使用 [asNoopFn] 会抛出异常，因为挂起函数的参数比参数列表还多一个挂起相关标识。
 */
fun <T : Any> KFunction<T>.asSuspendNoopFn(instance: Any? = null): suspend (TParams) -> T {
    require(this.isSuspend) { "The function type is incorrect, and it must be a 'suspend' function" }
    suspend fun run(params: TParams): T =
        this.callSuspend(*synthesisParametersAndCheck(instance, params, this))
    return ::run
}

internal fun <T> synthesisParametersAndCheck(instance: Any?, params: TParams, fn: KFunction<T?>): TParams {
    val finalParams = instance?.let { arrayOf(it, *params) } ?: params
    checkIsLegalParameters(fn, *finalParams)
    return finalParams
}

internal typealias PauseFn = KFunction0<Unit>
internal typealias ResumeFn = KFunction0<Unit>
internal typealias IsActive = Boolean

internal typealias ToggleFn = () -> Unit

internal typealias SetValueFn<T> = (T) -> Unit
internal typealias GetValueFn<T> = () -> T

internal typealias SuspendAsyncFn = suspend CoroutineScope.() -> Unit
