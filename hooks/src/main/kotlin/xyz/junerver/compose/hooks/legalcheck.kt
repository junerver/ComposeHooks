package xyz.junerver.compose.hooks

import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

/**
 * Description:
 * @author Junerver
 * date: 2024/1/31-10:38
 * Email: junerver@gmail.com
 * Version: v1.0
 */

/**
 * 用于检测包装函数是否合法，例如：
 * ```
 * fun originFn(string: String, i: Int): String {
 *     return "output: $string ----- $i"
 * }
 * fun originFnWraped(vararg prams: Any): String {
 *     checkIsLegalParameters(::originFn, *prams)
 *     return originFn(prams[0] as String, prams[1] as Int)
 * }
 * ```
 * 需要注意的一点是如果这是一个接口里的抽象方法，那么参数数量会比函数签名多一个，其类型就是接口本身。
 *
 *
 */
fun checkIsLegalParameters(fn: KFunction<*>, vararg params: Any?) {
    require(fn.parameters.size == params.size) {
        "Number of fn:${fn.name} parameters does not match. Expected: ${fn.parameters.size}, Actual: ${params.size}"
    }

    val realFnParams = fn.parameters

    for (i in realFnParams.indices) {
        // 函数的参数类型
        val fnParamType = realFnParams[i].type
        // 实际传入的参数
        val realParamType = params[i]?.javaClass?.kotlin?.createType()!!
        require(
            fnParamType.toString() == realParamType.toString() ||
                realParamType.isSubtypeOf(fnParamType)
        ) {
            "Parameter at index $i has incorrect type. Expected: $fnParamType, Actual: $realParamType"
        }
    }
}
