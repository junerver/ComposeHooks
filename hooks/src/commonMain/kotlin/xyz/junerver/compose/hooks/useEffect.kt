package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import xyz.junerver.compose.hooks.utils.unwrap

/*
  Description: Alias for [LaunchedEffect]
  Author: Junerver
  Date: 2024/3/4-8:20
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2024/9/18 10:50
  增加了解包装函数，对State、Ref进行解包，方便直接使用其实例作为依赖
*/

/**
 * 执行副作用的hook，通过设置[deps]依赖，可以在依赖发生变化时触发副作用闭包[block]的执行。
 * 依赖性会先执行[unwrap]进行解包装，所以它可以监听 [State] 、[Ref] 这两种包装类。
 *
 * By setting a [deps] dependency, you can trigger the execution of an effect
 * closure [block] when the dependency changes. The dependency will be unwrapped
 * first, so it can listen to both the [State] and [Ref] wrappers.
 */
@Composable
fun useEffect(vararg deps: Any?, block: SuspendAsyncFn) {
    LaunchedEffect(keys = unwrap(deps), block = block)
}
