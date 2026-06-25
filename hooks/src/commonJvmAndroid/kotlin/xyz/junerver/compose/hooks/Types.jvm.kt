package xyz.junerver.compose.hooks

import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import xyz.junerver.compose.hooks.utils.checkIsLegalParameters

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
 * @param mapParams 参数转换函数，你需要明确的告知如何将一个抽象的参数实例，转换成函数的参数列表数组
 */
fun <TParams, T> KFunction<T?>.asNoopFn(instance: Any? = null, mapParams: (TParams) -> Array<Any?> = ::defaultTupleMap): (TParams) -> T? =
    fun(params: TParams): T? = this.call(
        *synthesisParametersAndCheck(instance, mapParams(params), this),
    )

/**
 * 将一个 [suspend] 挂起函数转换成[suspend] 版本的 [NormalFunction] 函数。
 * 直接使用 [asNoopFn] 会抛出异常，因为挂起函数的参数比参数列表还多一个挂起相关标识。
 * 如果你没有显式的传递 [mapParams] 参数，那么 [mapParams] 会默认使用 [defaultTupleMap]，
 * 即将原函数的参数列表转换成 [Tuple]，注意不是具体的 TupleN，而是顶级接口 [Tuple]。
 */
fun <TParams, T : Any> KFunction<T>.asSuspendNoopFn(
    instance: Any? = null,
    mapParams: (TParams) -> Array<Any?> = ::defaultTupleMap,
): suspend (TParams) -> T {
    require(this.isSuspend) { "The function type is incorrect, and it must be a 'suspend' function" }

    suspend fun run(params: TParams): T = this.callSuspend(*synthesisParametersAndCheck(instance, mapParams(params), this))
    return ::run
}

internal fun <T> synthesisParametersAndCheck(instance: Any?, params: Array<Any?>, fn: KFunction<T?>): Array<Any?> {
    val finalParams = instance?.let { arrayOf(it, *params) } ?: params
    checkIsLegalParameters(fn, *finalParams)
    return finalParams
}

/**
 * 默认的参数映射，默认使用 [Tuple] 包装参数
 */
internal fun <TParams> defaultTupleMap(params: TParams): Array<Any?> = when (params) {
    is Array<*> -> arrayOf(*params)
    is None -> emptyArray()
    is Tuple1<*> -> arrayOf(params.first)
    is Tuple2<*, *> -> arrayOf(params.first, params.second)
    is Tuple3<*, *, *> -> arrayOf(params.first, params.second, params.third)
    is Tuple4<*, *, *, *> -> arrayOf(params.first, params.second, params.third, params.fourth)
    is Tuple5<*, *, *, *, *> -> arrayOf(params.first, params.second, params.third, params.fourth, params.fifth)
    is Tuple6<*, *, *, *, *, *> -> arrayOf(params.first, params.second, params.third, params.fourth, params.fifth, params.sixth)
    is Tuple7<*, *, *, *, *, *, *> -> arrayOf(
        params.first,
        params.second,
        params.third,
        params.fourth,
        params.fifth,
        params.sixth,
        params.seventh,
    )

    is Tuple8<*, *, *, *, *, *, *, *> -> arrayOf(
        params.first,
        params.second,
        params.third,
        params.fourth,
        params.fifth,
        params.sixth,
        params.seventh,
        params.eighth,
    )

    is Tuple9<*, *, *, *, *, *, *, *, *> -> arrayOf(
        params.first,
        params.second,
        params.third,
        params.fourth,
        params.fifth,
        params.sixth,
        params.seventh,
        params.eighth,
        params.ninth,
    )

    else -> throw IllegalArgumentException("The parameter abstract type you provide is not a tuple type")
}
