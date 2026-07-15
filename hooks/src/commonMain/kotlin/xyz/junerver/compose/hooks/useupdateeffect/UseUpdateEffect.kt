package xyz.junerver.compose.hooks.useupdateeffect

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.usecreation.useCreationImpl
import xyz.junerver.compose.hooks.useeffect.useEffectImpl
import xyz.junerver.compose.hooks.uselatest.useLatestRefImpl
import xyz.junerver.compose.hooks.utils.unwrap

/*
  Description: [useUpdateEffect] 用法等同于 [useEffect]，但是会忽略首次执行，只在依赖更新时执行。
  Author: Junerver
  Date: 2024/3/11-11:19
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useUpdateEffectImpl(vararg deps: Any?, block: SuspendAsyncFn) {
    val dependencyKey = useCreationImpl(*unwrap(deps)) { Any() }.current
    val initialDependencyKey = useCreationImpl { dependencyKey }.current
    val blockRef = useLatestRefImpl(block)
    useEffectImpl(dependencyKey) {
        if (dependencyKey !== initialDependencyKey) {
            blockRef.current.invoke(this)
        }
    }
}
