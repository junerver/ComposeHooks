package xyz.junerver.compose.hooks

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
