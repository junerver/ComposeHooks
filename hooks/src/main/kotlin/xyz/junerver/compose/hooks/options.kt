package xyz.junerver.compose.hooks

import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import xyz.junerver.kotlin.Tuple3
import xyz.junerver.kotlin.tuple

/**
 * Description: 规范Options的形式
 *
 * 需要作为选项配置的 `data class` 只需要添加一个伴生对象即可：
 * ```
 *  companion object : Options<DebounceOptions>(::DebounceOptions)
 * ```
 * 使用的时候也非常方便，直接通过[optionsOf]或者[defaultOption]这两个函数即可创建选项实例。
 * @author Junerver
 * date: 2024/2/1-8:08
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class Options<T>(val creator: () -> T) {
    /**
     * [optionOf]函数通过[apply]构造模式来修改默认参数对象
     */
    fun optionOf(opt: T.() -> Unit): T = creator().apply {
        opt()
    }

    /**
     * [default]函数直接调用构造器函数获取默认实例
     */
    fun default() = creator()
}

/**
 * 创建两个高阶函数，自动调用配置选项类的伴生对象对应函数，实现统一配置入口。
 * 如果需要默认配置对象直接使用 [defaultOption]，如果需要修改则直接使用[optionsOf]
 */
inline fun <reified T> optionsOf(noinline opt: T.() -> Unit): T {
    val (companionObj, _, optionOfMethod) = checkCompanionObject<T>()
    return optionOfMethod.call(companionObj, opt) as T
}

inline fun <reified T> defaultOption(): T {
    val (companionObj, defaultMethod, _) = checkCompanionObject<T>()
    return defaultMethod.call(companionObj) as T
}

/**
 * 检查伴生对象是否实现了 [Options] 抽象类。
 */
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
