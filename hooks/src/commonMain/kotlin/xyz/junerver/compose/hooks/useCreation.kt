package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/*
  Description:
  Author: Junerver
  Date: 2024/2/7-14:20
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Description: [useCreation] 是 [useRef] 的替代品 。而相比于 [useRef]，你可以使用
 * [useCreation] 创建一些常量， 这些常量和 [useRef] 创建出来的 ref 有很多使用场景上的相似，
 * 但对于复杂常量的创建，[useRef] 却容易出现潜在的性能隐患。
 *
 * [useCreation] is a replacement for [useRef]. Compared with [useRef], you
 * can use [useCreation] to create some constants. These constants have
 * many usage scenarios similar to the refs created by [useRef]. However,
 * for the creation of complex constants, [useRef] is prone to potential
 * errors. performance hazards.
 */
@Composable
fun <T> useCreation(vararg keys: Any?, factory: () -> T): Ref<T> = remember(keys = keys) {
    MutableRef(factory())
}
