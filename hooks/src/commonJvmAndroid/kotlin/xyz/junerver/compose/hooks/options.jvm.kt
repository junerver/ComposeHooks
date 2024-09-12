package xyz.junerver.compose.hooks

import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import xyz.junerver.kotlin.Tuple3
import xyz.junerver.kotlin.tuple

/*
  Description:
  Author: Junerver
  Date: 2024/2/1-8:08
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * 通过反射自动调用配置选项类的伴生对象对应函数，实现统一配置入口。 如果需要默认配置对象直接使用
 * [defaultOption]，如果需要修改则直接使用[optionsOf]
 */
actual inline fun <reified T> optionsOf(noinline opt: T.() -> Unit): T {
    val (companionObj, _, optionOfMethod) = checkCompanionObject<T>()
    return optionOfMethod.call(companionObj, opt) as T
}

/** [defaultOption]函数通过调用目标配置选项的[Options.default]函数，创建默认选项。 */
actual inline fun <reified T> defaultOption(): T {
    val (companionObj, defaultMethod, _) = checkCompanionObject<T>()
    return defaultMethod.call(companionObj) as T
}

/** 检查伴生对象是否实现了 [Options] 抽象类。 */
inline fun <reified T> checkCompanionObject(): Tuple3<Any, KFunction<*>, KFunction<*>> {
    val companionObj = T::class.companionObjectInstance
    require(companionObj != null) {
        "配置选项的数据类必须实现一个伴生对象"
    }
    val companionKClass = companionObj::class
    val defaultMethod = companionKClass.functions.firstOrNull { it.name == "default" }
    val optionOfMethod = companionKClass.functions.firstOrNull { it.name == "optionOf" }
    require(defaultMethod != null && optionOfMethod != null) {
        "伴生对象必须实现Options抽象类，或者自行实现其接口"
    }

    return tuple(companionObj, defaultMethod, optionOfMethod)
}
