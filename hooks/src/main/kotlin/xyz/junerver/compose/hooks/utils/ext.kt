package xyz.junerver.compose.hooks.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks.observeAsState

/*
  Description:
  Author: Junerver
  Date: 2024/8/1-10:44
  Email: junerver@gmail.com
  Version: v1.0
*/

internal val currentTime: Instant
    get() = Clock.System.now()

/**
 * 直接使用[State]、[Ref] 作为依赖项无法触发副作用闭包执行，只有状态值、props入参变化才能正常触发，
 * 所以需要执行解包装操作，简化对副作用使用时的心智负担。
 *
 * 注意：只能监听 [Ref] 实例，如果你使用 `by` 关键字进行了对 [Ref] 的委托，将无法通过 [useEffect] 监听。
 */
@Composable
internal inline fun unwrap(deps: Array<out Any?>) = deps.map {
    when (it) {
        is State<*> -> it.value
        is Ref<*> -> it.observeAsState().value
        is SnapshotStateList<*> -> it.toList()
        else -> it
    }
}.toTypedArray()
