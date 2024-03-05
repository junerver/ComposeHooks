package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Description: [useCreation] 是 useMemo 或 [useRef] 的替代品
 * 。而相比于 [useRef]，你可以使用 [useCreation] 创建一些常量，
 * 这些常量和 [useRef] 创建出来的 ref 有很多使用场景上的相似，
 * 但对于复杂常量的创建，[useRef] 却容易出现潜在的性能隐患。
 * @author Junerver
 * date: 2024/2/7-14:20
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun <T> useCreation(vararg keys: Any?, factory: () -> T): Ref<T> {
    val ref = rememberSaveable(inputs = keys) {
        Ref(factory())
    }
    return ref
}